package com.example.user_service.User_Mappers;

import com.example.user_service.Entites.Address;
import com.example.user_service.User_DTOs.AddressDTO;
import org.springframework.stereotype.Component;

@Component
public class AddressMappers {

    public Address MpaAddressDtoTOAddress(AddressDTO addressDTO) {
        return Address.builder()
                .addressLine1(addressDTO.getAddressLine1())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .country(addressDTO.getCountry())
                .pinCode(addressDTO.getPinCode())
                .addressType(addressDTO.getAddressType())
                .build();
    }

    public AddressDTO MpaAddressTOAddressDto(Address address) {
        return AddressDTO.builder()
                .addressLine1(address.getAddressLine1())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pinCode(address.getPinCode())
                .addressType(address.getAddressType())
                .build();
    }
}
