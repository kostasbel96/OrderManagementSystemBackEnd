package com.project.ordermanagementsystem.repository;

public interface PersonRepository {

    Boolean existsByPhoneNumber1AndActiveTrue(String phoneNumber1);

}
