package com.example.project04mongodb_crud.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "users")
public class User {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String companyName;
    private int experienceMonths;
    private String mobileNumber;

    public User(String userId, String firstName, String lastName, String companyName, int experienceMonths, String mobileNumber){
        this.userId = userId;
        this.firstName=firstName;
        this.lastName=lastName;
        this.companyName=companyName;
        this.experienceMonths=experienceMonths;
        this.mobileNumber=mobileNumber;
    }

    public User() {}

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getExperienceMonths() {
        return experienceMonths;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setExperienceMonths(int experienceMonths) {
        this.experienceMonths = experienceMonths;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return userId != null && userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }


    @Override
    public String toString() {
        return String.format(
                "UserID: %s\nFirst Name: %s\nLast Name: %s\nCompany: %s\nExp Months: %d\nMobile: %s",
                userId,
                firstName,
                lastName,
                companyName,
                experienceMonths,
                mobileNumber
        );
    }

}
