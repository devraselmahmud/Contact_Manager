package com.returncontinue.contactapi.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.returncontinue.contactapi.domain.Contact;
import com.returncontinue.contactapi.repo.ContactRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static com.returncontinue.contactapi.constant.Constant.PHOTO_DIRECTORY;;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {

	private final ContactRepo contactRepo;
	
	public Page<Contact> getAllContacts(int page, int size){
		return contactRepo.findAll(PageRequest.of(page,  size, Sort.by("name")));
	}
	
	public Contact getContacts(String id) {
		return contactRepo.findById(id).orElseThrow(() -> new RuntimeException("Contact not found"));
	}
	
	public Contact createContact(Contact contact) {
		return contactRepo.save(contact);
	}
	
	public void deleteContact(Contact contact) {
		contactRepo.delete(contact);
	}
	
	public String uploadPhoto(String id, MultipartFile file) {
		
		log.info("Saving picture for user ID: {}", id);
		
		Contact contact = getContacts(id);
		String photoUrl = photoFunction.apply(id, file);
		contact.setPhotoUrl(photoUrl);
		contactRepo.save(contact);
		return photoUrl;
	}
	
	private final Function<String, String> fileExtension = filename -> (
		Optional.of(filename).filter(name -> name.contains("."))
		.map(name -> "." + name.substring(filename.lastIndexOf(".") + 1)).orElse(".png")
	);
	
	private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
		String filename = id + fileExtension.apply(image.getOriginalFilename());
		
		try {
			Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
			if(Files.exists(fileStorageLocation)) {
				Files.createDirectories(fileStorageLocation);
			}
			
			Files.copy(image.getInputStream(), fileStorageLocation
					.resolve(id + fileExtension.apply(image.getOriginalFilename())), REPLACE_EXISTING);
			
			return ServletUriComponentsBuilder
					.fromCurrentContextPath()
					.path("/contacts/image/" + filename).toString();
			
		}catch(Exception exception) {
			throw new RuntimeException("Unable to save image");
		}
	};
	

}
