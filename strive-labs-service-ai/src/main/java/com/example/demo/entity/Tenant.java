package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;

@Entity
@Table(name = "tenants")
public class Tenant {

	@Id
	@Column(name = "tenant_id", nullable = false, unique = true, length = 50)
	private String tenantId; // Tenant ID as a VARCHAR(50)

	@Column(name = "storage_limit", nullable = false)
	private long storageLimit; // Storage limit in bytes (maps to BIGINT in SQL)

	// Getters and setters
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public long getStorageLimit() {
		return storageLimit;
	}

	public void setStorageLimit(long storageLimit) {
		this.storageLimit = storageLimit;
	}
}
