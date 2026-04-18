package com.example.user_service.User_DTOs;

import com.example.user_service.Entites.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDto {

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private AddressDTO address;

    private UserRole userRole;
}
