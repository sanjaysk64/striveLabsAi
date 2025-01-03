package com.example.demo.ttl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.repository.KeyValueRepository;

@Component
public class TTLTask {
	private final KeyValueRepository repository;

	public TTLTask(KeyValueRepository repository) {
		this.repository = repository;
	}

	@Scheduled(fixedRate = 60000)
	public void cleanupExpiredKeys() {
		LocalDateTime now = LocalDateTime.now();
		repository.markExpiredKeys(now);

	}
}
