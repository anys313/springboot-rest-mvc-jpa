package com.appsdeveloper.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloper.app.ws.exceptions.UserServiceException;
import com.appsdeveloper.app.ws.service.AddressService;
import com.appsdeveloper.app.ws.service.UserService;
import com.appsdeveloper.app.ws.shared.dto.AddressDTO;
import com.appsdeveloper.app.ws.shared.dto.UserDto;
import com.appsdeveloper.app.ws.ui.model.request.UserDetailsRequestModel;
import com.appsdeveloper.app.ws.ui.model.response.AddressesRest;
import com.appsdeveloper.app.ws.ui.model.response.ErrorMessages;
import com.appsdeveloper.app.ws.ui.model.response.OperationStatusModel;
import com.appsdeveloper.app.ws.ui.model.response.RequestOperationName;
import com.appsdeveloper.app.ws.ui.model.response.RequestOperationStatus;
import com.appsdeveloper.app.ws.ui.model.response.UserRest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/users") // http://localhost:8080/users
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	// @RolesAllowed
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();

		UserDto userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);

		return returnValue;
	}

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		// if(userDetails.getFirstName().isEmpty()) throw new NullPointerException("Null
		// pointer found");

		// UserDto userDto = new UserDto();
		// BeanUtils.copyProperties(userDetails, userDto);
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);

		UserDto createdUser = userService.createUser(userDto);
		UserRest returnValue = new UserRest();

		// BeanUtils.copyProperties(createdUser, returnValue);
		returnValue = modelMapper.map(createdUser, UserRest.class);
		return returnValue;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		// if(userDetails.getFirstName().isEmpty()) throw new
		// UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage())
		// ;
		// if(userDetails.getFirstName().isEmpty()) throw new NullPointerException("Null
		// pointer found");

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updatedUser = userService.updateUser(id, userDto);
		UserRest returnValue = new UserRest();

		BeanUtils.copyProperties(updatedUser, returnValue);
		return returnValue;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());

		userService.deleteUser(id);
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}

	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {
		List<UserRest> returnValue = new ArrayList<>();
		List<UserDto> users = userService.getUsers(page, limit);
		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}
		return returnValue;
	}

	// mobile-app-ws/users/{id}/addresses
	@GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE, "application/hal+json" , "application/hal+xml" })
	// @RolesAllowed
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> addressesListRestModel = new ArrayList<AddressesRest>();

		// UserDto userDto = userService.getUserByUserId(id);
		// BeanUtils.copyProperties(userDto, returnValue);
		List<AddressDTO> addressesDTO = addressService.getAddresses(id);
		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			java.lang.reflect.Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			addressesListRestModel = new ModelMapper().map(addressesDTO, listType);
			for(AddressesRest addressRest : addressesListRestModel) {
				Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(id,addressRest.getAddressId())).withRel("addresses");
				addressRest.add(addressLink);
				
				Link userLink = linkTo(methodOn(UserController.class).getUser(id)).withRel("user");
				addressRest.add(userLink);
			}
		}
		return new CollectionModel<>(addressesListRestModel);
	}

	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE, "application/hal+json" , "application/hal+xml"  })
	// @RolesAllowed
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {

		AddressDTO addressDTO = addressService.getAddress(addressId);
		//Link link = linkTo(UserController.class).slash(userId).slash("addresses").slash(addressId).withSelfRel();
		Link link = linkTo(methodOn(UserController.class).getUserAddress(userId, addressId)).withSelfRel();
		Link userLink = linkTo(UserController.class).slash(userId).withRel("user");
		//Link addressesLink = linkTo(UserController.class).slash(userId).slash("addresses").withRel("addresses");
				
		Link addressesLink = linkTo(methodOn(UserController.class).getUserAddresses(userId)).withRel("addresses");
		AddressesRest returnValue = new ModelMapper().map(addressDTO, AddressesRest.class);
		
		returnValue.add(link);
		returnValue.add(userLink);
		returnValue.add(addressesLink);
		
		return new EntityModel<>(returnValue);

	}
}
