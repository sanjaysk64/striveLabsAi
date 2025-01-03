package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.DTO.KeyValueRequest;
import com.example.demo.entity.KeyValueStore;
import com.example.demo.service.KeyValueService;
import org.springframework.http.HttpStatus;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/")
public class KeyValueController {
	private final KeyValueService service;
	private static final int MAX_BATCH_SIZE = 100;

	public KeyValueController(KeyValueService service) {
		this.service = service;
	}

	@GetMapping("object/{key}")
	public ResponseEntity<?> getObject(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String key) {
		Optional<KeyValueStore> keyValue = service.getKeyValue(tenantId, key);
		return keyValue.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("object")
	public ResponseEntity<?> createObject(@RequestHeader("X-Tenant-ID") String tenantId,
			@RequestBody KeyValueRequest request) {
		KeyValueStore keyValue = service.createKeyValue(tenantId, request.getKey(), request.getData(),
				request.getTtl() != null ? LocalDateTime.now().plusSeconds(request.getTtl()) : null);
		return ResponseEntity.ok(keyValue);
	}

	@PostMapping("/batch/object")
	public ResponseEntity<?> createBatchObject(@RequestHeader("X-Tenant-ID") String tenantId,
			@RequestBody List<KeyValueRequest> requests) {
		if (requests.isEmpty()) {
			return ResponseEntity.badRequest().body("Batch cannot be empty.");
		}
		if (requests.size() > MAX_BATCH_SIZE) {
			return ResponseEntity.badRequest().body("Batch size exceeds the limit of " + MAX_BATCH_SIZE + " items.");
		}
		service.createBatchKeyValue(tenantId, requests);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("object/{key}")
	public ResponseEntity<?> deleteObject(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String key) {
		try {
			boolean isDeleted = service.deleteKeyValue(tenantId, key);
			if (isDeleted) {
				return ResponseEntity.noContent().build(); // 204 No Content
			}
			return ResponseEntity.status(HttpStatus.GONE).body("Record cannot be deleted."); // 410 Gone
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found for tenant."); // 404 Not Found
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

}
