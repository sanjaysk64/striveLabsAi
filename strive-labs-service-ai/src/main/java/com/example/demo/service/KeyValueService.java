package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.demo.DTO.KeyValueRequest;
import com.example.demo.entity.KeyValueStore;

public interface KeyValueService {
	Optional<KeyValueStore> getKeyValue(String tenantId, String key);

	KeyValueStore createKeyValue(String tenantId, String key, String data, LocalDateTime expiresAt);

	void createBatchKeyValue(String tenantId, List<KeyValueRequest> requests);

	boolean deleteKeyValue(String tenantId, String key);

}
