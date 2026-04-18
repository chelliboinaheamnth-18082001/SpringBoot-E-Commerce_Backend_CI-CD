package com.example.user_service.Services.Implementation;

import com.example.user_service.Entites.Address;
import com.example.user_service.Entites.Users;
import com.example.user_service.ExceptionHandlers.UserNotFoundException;
import com.example.user_service.Repositories.UserRepo;
import com.example.user_service.Services.KeyclockService.KeyClockUserService;
import com.example.user_service.Services.UserService;
import com.example.user_service.User_DTOs.UserRequestDto;
import com.example.user_service.User_DTOs.UserResponseDTO;
import com.example.user_service.User_Mappers.UserMappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMappers userMappers;
    private final KeyClockUserService keyCloakAdminService;

    @Override
    public UserResponseDTO createUser(UserRequestDto userRequestDto) {
//        String token = keyCloakAdminService.getAdminAccessToken();
//        String keycloakUserId =
//                keyCloakAdminService.createUser(token,userRequestDto);
        String email = userRequestDto.getEmail();
        String mobileNumber = userRequestDto.getMobileNumber();

        if (userRepo.existsByEmailOrMobileNumber(email,mobileNumber)) {
            throw new RuntimeException("User already exists with email or mobile number");
        }

        Users users = userMappers.MpaUsesDtoToUser(userRequestDto);
        String token = keyCloakAdminService.getAdminAccessToken();

        String keycloakUserId =
                keyCloakAdminService.createUser(token, userRequestDto);

        keyCloakAdminService.assignClientRoleToUser(
                token,   // ✅ reuse token
                userRequestDto.getUsername(),
                userRequestDto.getUserRole().toString(),
                keycloakUserId
        );
        users.setKeyCloakId(keycloakUserId);
        userRepo.save(users);
        UserResponseDTO userResponseDTO = userMappers.MpaUserToUserResponseDTO(users);
        return userResponseDTO;

    }

    @Override
    public UserResponseDTO getUserById(Long id) {

        Optional<Users> byId = userRepo.findById(id);
        if(byId.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+id);
        }
        Users users = byId.get();

        UserResponseDTO userResponseDTO = userMappers.MpaUserToUserResponseDTO(users);
        return userResponseDTO;
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDto userRequestDto) {
        Users users = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        users.setFirstName(userRequestDto.getFirstName());
        users.setLastName(userRequestDto.getLastName());
        users.setEmail(userRequestDto.getEmail());
        users.setMobileNumber(userRequestDto.getMobileNumber());

        // Update existing address instead of creating new
        Address existingAddress = users.getAddress();
        if (existingAddress != null) {
            existingAddress.setAddressLine1(userRequestDto.getAddress().getAddressLine1());
            existingAddress.setCity(userRequestDto.getAddress().getCity());
            existingAddress.setState(userRequestDto.getAddress().getState());
            existingAddress.setCountry(userRequestDto.getAddress().getCountry());
            existingAddress.setPinCode(userRequestDto.getAddress().getPinCode());
            existingAddress.setAddressType(userRequestDto.getAddress().getAddressType());
        } else {
            Address newAddress = userMappers.MpaAddressDtoTOAddress(userRequestDto.getAddress());
            users.setAddress(newAddress);
        }

        userRepo.save(users);

        return userMappers.MpaUserToUserResponseDTO(users);
    }

    @Override
    public void deleteUser(Long id) {
        Optional<Users> byId = userRepo.findById(id);
        if(byId.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+id);
        }
        userRepo.delete(byId.get());

    }

    @Override
    public List<UserResponseDTO> getAllUsers() {

        List<Users> allUsers = userRepo.findAll();
        if(allUsers.isEmpty())
        {
            throw new RuntimeException("Users Database is empty please try after some time");
        }
        return allUsers.stream()
                .map(user -> {
                    return userMappers.MpaUserToUserResponseDTO(user);
                })
                .collect(Collectors.toList());
    }


}
