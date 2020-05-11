package com.appsdeveloper.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsdeveloper.app.ws.exceptions.UserServiceException;
import com.appsdeveloper.app.ws.io.entity.AddressEntity;
import com.appsdeveloper.app.ws.io.entity.UserEntity;
import com.appsdeveloper.app.ws.io.repository.UserRepository;
import com.appsdeveloper.app.ws.service.UserService;
import com.appsdeveloper.app.ws.shared.Utils;
import com.appsdeveloper.app.ws.shared.dto.AddressDTO;
import com.appsdeveloper.app.ws.shared.dto.UserDto;
import com.appsdeveloper.app.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepository.findUserByEmail(user.getEmail()) != null) {
			throw new RuntimeException("Duplicate email found");
		}
		for (AddressDTO uAddr : user.getAddresses()) {
			uAddr.setUserDetails(user);
			uAddr.setAddressId(utils.generateAddressId(30));
		}

		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);

		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		UserEntity savedUser = userRepository.save(userEntity);

		// BeanUtils.copyProperties(savedUser, returnValue);
		UserDto returnValue = modelMapper.map(savedUser, UserDto.class);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findUserByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new User(userEntity.getEmail(), userEntity.getEncryptPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findUserByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findUserByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("User with id" + userId + " Not Found");

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto userDto) {
		UserEntity userEntity = userRepository.findUserByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(userDto.getFirstName());
		userEntity.setLastName(userDto.getLastName());

		UserEntity updatedUserEntity = userRepository.save(userEntity);

		UserDto returnValue = new UserDto();

		BeanUtils.copyProperties(updatedUserEntity, returnValue);

		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findUserByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		if (page > 0)
			page--;
		List<UserDto> returnValue = new ArrayList<>();
		Pageable pageableReqeust = PageRequest.of(page, limit);
		Page<UserEntity> usersPage = userRepository.findAll(pageableReqeust);
		List<UserEntity> users = usersPage.getContent();
		// List<UserEntity> users = (List<UserEntity>) userRepository.findAll();
		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}
		return returnValue;
	}

}
