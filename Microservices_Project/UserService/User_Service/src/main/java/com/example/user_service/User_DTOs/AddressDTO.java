package com.example.user_service.User_DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    private String addressLine1;

    private String city;

    private String state;

    private String country;

    private String pinCode;

    private String addressType;
}
