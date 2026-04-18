package com.example.user_service.User_Mappers;

import com.example.user_service.Entites.Address;
import com.example.user_service.Entites.Users;
import com.example.user_service.User_DTOs.AddressDTO;
import com.example.user_service.User_DTOs.UserRequestDto;
import com.example.user_service.User_DTOs.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMappers {
    private final AddressMappers addressMappers;

    public Users MpaUsesDtoToUser(UserRequestDto userRequest) {
        return Users.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .mobileNumber(userRequest.getMobileNumber())
                .address(addressMappers.MpaAddressDtoTOAddress(userRequest.getAddress()))
                .userRole(userRequest.getUserRole())
                .build();
    }

    public UserResponseDTO MpaUserToUserResponseDTO(Users users) {
        return UserResponseDTO.builder()
                .firstName(users.getFirstName())
                .lastName(users.getLastName())
                .email(users.getEmail())
                .mobileNumber(users.getMobileNumber())
                .address(addressMappers.MpaAddressTOAddressDto(users.getAddress()))
                .userRole(users.getUserRole())
                .build();
    }

    public Address MpaAddressDtoTOAddress(AddressDTO address) {
        return Address.builder()
                .addressLine1(address.getAddressLine1())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pinCode(address.getPinCode())
                .addressType(address.getAddressType())
                .build();
    }
}
