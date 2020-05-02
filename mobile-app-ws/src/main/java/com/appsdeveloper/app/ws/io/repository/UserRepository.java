package com.appsdeveloper.app.ws.io.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.appsdeveloper.app.ws.io.entity.UserEntity;

@Repository
public interface UserRepository extends PagingAndSortingRepository<UserEntity, Long> {
//public interface UserRepository extends CrudRepository<UserEntity, Long> {
	
	UserEntity findUserByEmail(String email);

	UserEntity findUserByUserId(String id);

}
