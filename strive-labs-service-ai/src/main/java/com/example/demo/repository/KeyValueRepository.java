package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.KeyValueStore;

import jakarta.transaction.Transactional;

@Repository
public interface KeyValueRepository extends JpaRepository<KeyValueStore, Long> {
	Optional<KeyValueStore> findByTenantIdAndKey(String tenantId, String key);

	@Transactional
	void deleteByTenantIdAndKey(String tenantId, String key);

	@Modifying
	@Transactional
	@Query("UPDATE KeyValueStore kv SET kv.expired = true WHERE kv.expiresAt < :now AND kv.expired = false")
	void markExpiredKeys(@Param("now") LocalDateTime now);

	List<KeyValueStore> findByTenantId(String tenantId);

}
