package com.returncontinue.contactapi.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.returncontinue.contactapi.domain.Contact;


public interface ContactRepo extends JpaRepository<Contact, String> {
	
	Optional<Contact> findById(String id);

}
