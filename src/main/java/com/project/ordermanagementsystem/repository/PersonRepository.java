package com.project.ordermanagementsystem.repository;

public interface PersonRepository {

    boolean existsByPhoneNumber1AndActiveTrue(String phoneNumber1);

}
