package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, String> {
	Optional<Tenant> findByTenantId(String tenantId);
}
