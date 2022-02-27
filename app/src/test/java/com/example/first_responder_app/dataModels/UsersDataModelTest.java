package com.example.first_responder_app.dataModels;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.truth.Truth;
import com.google.firebase.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsersDataModelTest {

    String address, first_name, last_name, password, rank, username, remote_path_to_profile_picture;
    long phone_number;
    Timestamp responding_time;

    @BeforeEach
    void setUp() {
        address = "123 fake address";
        first_name = "first_name";
        last_name = "last_name";
        password = "password1";
        phone_number = 1234567890L;
        rank = "rankId";
        username = "username";
        remote_path_to_profile_picture = "remotePath";
    }

    /** getFull_name Tests **/

    @Test
    void getFull_nameHardCoded1_returnsTrue() {
        UsersDataModel user = new UsersDataModel(address, first_name, last_name, password, phone_number, rank, username, responding_time, remote_path_to_profile_picture);
        String fullName = first_name + " " + last_name;

        Truth.assertThat(fullName.equals(user.getFull_name())).isTrue();
    }

    @Test
    void getFull_nameHardCoded2_returnsTrue() {
        first_name = "NewFirstName";
        last_name = "NewLastName";
        UsersDataModel user = new UsersDataModel(address, first_name, last_name, password, phone_number, rank, username, responding_time, remote_path_to_profile_picture);
        String fullName = first_name + " " + last_name;

        Truth.assertThat(fullName.equals(user.getFull_name())).isTrue();
    }
}