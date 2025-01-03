package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.DTO.KeyValueRequest;
import com.example.demo.entity.KeyValueStore;
import com.example.demo.entity.Tenant;
import com.example.demo.repository.KeyValueRepository;
import com.example.demo.repository.TenantRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class KeyValueServiceImpl implements KeyValueService {

	private final KeyValueRepository repository;
	private static final int MAX_BATCH_SIZE = 100;
	private final TenantRepository tenantRepository; // Tenant repository for storage limits

	public KeyValueServiceImpl(KeyValueRepository repository, TenantRepository tenantRepository) {
		this.repository = repository;
		this.tenantRepository = tenantRepository;
	}

	@Transactional(readOnly = true)
	public Optional<KeyValueStore> getKeyValue(String tenantId, String key) {
		Optional<KeyValueStore> keyValue = repository.findByTenantIdAndKey(tenantId, key);
		if (keyValue.isPresent()) {
			KeyValueStore kvs = keyValue.get();
			if (kvs.getExpired()) {
				return Optional.empty();
			}
			if (kvs.getExpiresAt() != null && kvs.getExpiresAt().isBefore(LocalDateTime.now()) && !kvs.getExpired()) {
				kvs.setExpired(true);
				repository.save(kvs);
				return Optional.empty();
			}
			return keyValue;
		} else {
			return Optional.empty();
		}
	}

	@Transactional
	public KeyValueStore createKeyValue(String tenantId, String key, String data, LocalDateTime expiresAt) {
		try {
			Tenant tenant = tenantRepository.findById(tenantId)
					.orElseThrow(() -> new IllegalArgumentException("Invalid tenant ID"));

			long currentSize = calculateTenantTotalSize(tenantId);
			long newEntrySize = calculateSize(key, data);

			if (currentSize + newEntrySize > tenant.getStorageLimit()) {
				throw new IllegalStateException("Storage limit exceeded for tenant: " + tenantId);
			}

			if (repository.findByTenantIdAndKey(tenantId, key).isPresent()) {
				throw new IllegalArgumentException("Key already exists");
			}

			KeyValueStore keyValue = new KeyValueStore();
			keyValue.setTenantId(tenantId);
			keyValue.setKey(key);
			keyValue.setData(data);
			keyValue.setExpiresAt(expiresAt);
			return repository.save(keyValue);
		} catch (OptimisticLockingFailureException e) {
			throw new IllegalStateException("Failed to create or update due to a conflict. Please try again.");

		}
	}

	@Transactional
	public void createBatchKeyValue(String tenantId, List<KeyValueRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			throw new IllegalArgumentException("Request list cannot be null or empty.");
		}
		if (requests.size() > MAX_BATCH_SIZE) {
			throw new IllegalArgumentException("Batch size exceeds the limit of " + MAX_BATCH_SIZE + " items.");
		}
		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid tenant ID"));
		long batchTotalSize = requests.stream().mapToLong(request -> calculateSize(request.getKey(), request.getData()))
				.sum();
		long currentTenantSize = calculateTenantTotalSize(tenantId);
		if (currentTenantSize + batchTotalSize > tenant.getStorageLimit()) {
			throw new IllegalStateException("Storage limit exceeded for tenant: " + tenantId);
		}
		List<KeyValueStore> keyValueStores = requests.stream().map(request -> {
			KeyValueStore keyValueStore = new KeyValueStore();
			keyValueStore.setTenantId(tenantId);
			keyValueStore.setKey(request.getKey());
			keyValueStore.setData(request.getData());
			keyValueStore
					.setExpiresAt(request.getTtl() != null ? LocalDateTime.now().plusSeconds(request.getTtl()) : null);
			return keyValueStore;
		}).toList();

		repository.saveAll(keyValueStores);
	}

	@Transactional
	public boolean deleteKeyValue(String tenantId, String key) {
		try {
			Optional<KeyValueStore> keyValue = repository.findByTenantIdAndKey(tenantId, key);
			if (keyValue.isPresent()) {
				KeyValueStore kvs = keyValue.get();
				if (kvs.getExpiresAt() != null && !kvs.getExpired()) {
					repository.deleteByTenantIdAndKey(tenantId, key);
					return true; // Successfully deleted
				}
				return false; // Record is expired
			} else {
				throw new EntityNotFoundException("Key not found for tenant ID: " + tenantId);
			}
		} catch (OptimisticLockingFailureException e) {
			throw new IllegalStateException("Failed to delete due to a conflict. Please try again.", e);
		}
	}

	private long calculateTenantTotalSize(String tenantId) {
		return repository.findByTenantId(tenantId).stream().mapToLong(kv -> calculateSize(kv.getKey(), kv.getData()))
				.sum();
	}

	private long calculateSize(String key, String data) {
		return key.getBytes().length + data.getBytes().length;
	}

}
