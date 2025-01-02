package com.example.demo.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.example.demo.entity.UserCredential;
import com.example.demo.repo.UserCredentialRepository;

@Component
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserCredentialRepository repository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserCredential> credential = repository.findByName(username);
		return credential.map(CustomUserDetails::new)// convert UserCredential to user detail
				.orElseThrow(() -> new UsernameNotFoundException("user not found with name :" + username));
	}
}
