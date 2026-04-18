package com.example.user_service.Services.Implementation;

import com.example.user_service.Entites.Address;
import com.example.user_service.Entites.UserRole;
import com.example.user_service.Entites.Users;
import com.example.user_service.ExceptionHandlers.UserNotFoundException;
import com.example.user_service.Repositories.UserRepo;
import com.example.user_service.Services.KeyclockService.KeyClockUserService;
import com.example.user_service.User_DTOs.AddressDTO;
import com.example.user_service.User_DTOs.UserRequestDto;
import com.example.user_service.User_DTOs.UserResponseDTO;
import com.example.user_service.User_Mappers.UserMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImplTest")
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private UserMappers userMappers;

    @Mock
    private KeyClockUserService keyCloakAdminService;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private UserRequestDto userRequestDto;

    private UserResponseDTO expectedResponse;

    @BeforeEach
    public void initialisationInputs() {
        userRequestDto = UserRequestDto.builder()
                .username("john_doe")
                .password("securePass123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .mobileNumber("9876543210")
                .address(AddressDTO.builder()
                        .addressLine1("123 Main Street")
                        .city("Bengaluru")
                        .state("Karnataka")
                        .country("India")
                        .pinCode("560001")
                        .addressType("Home")
                        .build())
                .userRole(UserRole.USER)
                .build();

        expectedResponse = UserResponseDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .mobileNumber("9876543210")
                .address(userRequestDto.getAddress())
                .userRole(UserRole.USER)
                .build();

    }



    @Nested
    @DisplayName("createUserTest")
    class CreateUserTestClass
    {
        @Test
        @DisplayName("Should create user successfully")
        void testCreateUser() {
            // Arrange
            Users users = new Users();
            users.setFirstName("John");
            users.setLastName("Doe");
            users.setEmail("john.doe@example.com");
            users.setMobileNumber("9876543210");

            String token = "mocked-token";
            String keycloakUserId = "mocked-keycloak-id";

            // Mock behavior
            when(userRepo.existsByEmailOrMobileNumber(
                    userRequestDto.getEmail(),
                    userRequestDto.getMobileNumber()))
                    .thenReturn(false);

            when(userMappers.MpaUsesDtoToUser(userRequestDto))
                    .thenReturn(users);

            when(keyCloakAdminService.getAdminAccessToken())
                    .thenReturn(token);

            when(keyCloakAdminService.createUser(token, userRequestDto))
                    .thenReturn(keycloakUserId);

            when(userRepo.save(users))
                    .thenReturn(users);

            when(userMappers.MpaUserToUserResponseDTO(users)).thenReturn(expectedResponse);

            // Act
            UserResponseDTO actualResponse = userServiceImpl.createUser(userRequestDto);

            // Assert
            assertNotNull(actualResponse);

            assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
            assertEquals(expectedResponse.getLastName(), actualResponse.getLastName());
            assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
            assertEquals(expectedResponse.getMobileNumber(), actualResponse.getMobileNumber());
            assertEquals(expectedResponse.getUserRole(), actualResponse.getUserRole());

            // Verify interactions
            verify(userRepo, times(1))
                    .existsByEmailOrMobileNumber(userRequestDto.getEmail(), userRequestDto.getMobileNumber());

            verify(userMappers, times(1))
                    .MpaUsesDtoToUser(userRequestDto);

            verify(keyCloakAdminService, times(1))
                    .getAdminAccessToken();

            verify(keyCloakAdminService, times(1))
                    .createUser(token, userRequestDto);

            verify(keyCloakAdminService, times(1))
                    .assignClientRoleToUser(token,
                            userRequestDto.getUsername(),
                            userRequestDto.getUserRole().toString(),
                            keycloakUserId);

            verify(userRepo, times(1)).save(users);

            verify(userMappers, times(1))
                    .MpaUserToUserResponseDTO(users);
        }

        @Test
        @DisplayName("Should throw exception when user already exists")
        void testCreateUser_UserAlreadyExists() {
            // Arrange
            when(userRepo.existsByEmailOrMobileNumber(
                    userRequestDto.getEmail(),
                    userRequestDto.getMobileNumber()))
                    .thenReturn(true);

            // Act & Assert
            RuntimeException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                            () -> userServiceImpl.createUser(userRequestDto));

            assertEquals("User already exists with email or mobile number", exception.getMessage());

            // Verify that no other mocks were called
            verify(userMappers, times(0)).MpaUsesDtoToUser(any());
            verify(keyCloakAdminService, times(0)).getAdminAccessToken();
            verify(keyCloakAdminService, times(0)).createUser(anyString(), any());
            verify(keyCloakAdminService, times(0)).assignClientRoleToUser(anyString(), anyString(), anyString(), anyString());
            verify(userRepo, times(0)).save(any());
            verify(userMappers, times(0)).MpaUserToUserResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("updateUserTest")
    class UpdateUserTestClass {

        private Users existingUser;

        @BeforeEach
        void setupExistingUser() {
            existingUser = new Users();
            existingUser.setId(1L);
            existingUser.setFirstName("OldFirst");
            existingUser.setLastName("OldLast");
            existingUser.setEmail("old.email@example.com");
            existingUser.setMobileNumber("1234567890");

            // Existing address
            Address existingAddress = new Address();
            existingAddress.setAddressLine1("Old Street");
            existingAddress.setCity("OldCity");
            existingAddress.setState("OldState");
            existingAddress.setCountry("OldCountry");
            existingAddress.setPinCode("000000");
            existingAddress.setAddressType("Office");

            existingUser.setAddress(existingAddress);
        }

        @Test
        @DisplayName("Should update user successfully when user exists")
        void testUpdateUserSuccess() {
            // Arrange
            when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(existingUser));
            when(userRepo.save(existingUser)).thenReturn(existingUser);
            when(userMappers.MpaUserToUserResponseDTO(existingUser)).thenReturn(expectedResponse);

            // Act
            UserResponseDTO actualResponse = userServiceImpl.updateUser(1L, userRequestDto);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
            assertEquals(expectedResponse.getLastName(), actualResponse.getLastName());
            assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
            assertEquals(expectedResponse.getMobileNumber(), actualResponse.getMobileNumber());

            // Verify interactions
            verify(userRepo, times(1)).findById(1L);
            verify(userRepo, times(1)).save(existingUser);
            verify(userMappers, times(1)).MpaUserToUserResponseDTO(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void testUpdateUser_UserNotFound() {
            // Arrange
            when(userRepo.findById(99L)).thenReturn(java.util.Optional.empty());

            // Act & Assert
            UserNotFoundException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(UserNotFoundException.class,
                            () -> userServiceImpl.updateUser(99L, userRequestDto));

            assertEquals("User not found with id: 99", exception.getMessage());

            // Verify that save and mapper are never called
            verify(userRepo, times(1)).findById(99L);
            verify(userRepo, times(0)).save(any());
            verify(userMappers, times(0)).MpaUserToUserResponseDTO(any());
        }
    }


    @Nested
    @DisplayName("getUserByIdTest")
    class GetUserByIdTestClass {

        private Users existingUser;

        @BeforeEach
        void setupExistingUser() {
            existingUser = new Users();
            existingUser.setId(1L);
            existingUser.setFirstName("John");
            existingUser.setLastName("Doe");
            existingUser.setEmail("john.doe@example.com");
            existingUser.setMobileNumber("9876543210");
            existingUser.setAddress(userMappers.MpaAddressDtoTOAddress(userRequestDto.getAddress()));
        }

        @Test
        @DisplayName("Should return user when found")
        void testGetUserByIdSuccess() {
            // Arrange
            when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(existingUser));
            when(userMappers.MpaUserToUserResponseDTO(existingUser)).thenReturn(expectedResponse);

            // Act
            UserResponseDTO actualResponse = userServiceImpl.getUserById(1L);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
            assertEquals(expectedResponse.getLastName(), actualResponse.getLastName());
            assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
            assertEquals(expectedResponse.getMobileNumber(), actualResponse.getMobileNumber());

            // Verify interactions
            verify(userRepo, times(1)).findById(1L);
            verify(userMappers, times(1)).MpaUserToUserResponseDTO(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void testGetUserById_UserNotFound() {
            // Arrange
            when(userRepo.findById(99L)).thenReturn(java.util.Optional.empty());

            // Act & Assert
            UserNotFoundException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(UserNotFoundException.class,
                            () -> userServiceImpl.getUserById(99L));

            assertEquals("User not found with id: 99", exception.getMessage());

            // Verify interactions
            verify(userRepo, times(1)).findById(99L);
            verify(userMappers, times(0)).MpaUserToUserResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("deleteUserTest")
    class DeleteUserTestClass {

        private Users existingUser;

        @BeforeEach
        void setupExistingUser() {
            existingUser = new Users();
            existingUser.setId(1L);
            existingUser.setFirstName("John");
            existingUser.setLastName("Doe");
            existingUser.setEmail("john.doe@example.com");
            existingUser.setMobileNumber("9876543210");
        }

        @Test
        @DisplayName("Should delete user successfully when user exists")
        void testDeleteUserSuccess() {
            // Arrange
            when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(existingUser));

            // Act
            userServiceImpl.deleteUser(1L);

            // Assert & Verify
            verify(userRepo, times(1)).findById(1L);
            verify(userRepo, times(1)).delete(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void testDeleteUser_UserNotFound() {
            // Arrange
            when(userRepo.findById(99L)).thenReturn(java.util.Optional.empty());

            // Act & Assert
            UserNotFoundException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(UserNotFoundException.class,
                            () -> userServiceImpl.deleteUser(99L));

            assertEquals("User not found with id: 99", exception.getMessage());

            // Verify interactions
            verify(userRepo, times(1)).findById(99L);
            verify(userRepo, times(0)).delete(any());
        }
    }


    @Nested
    @DisplayName("getAllUsersTest")
    class GetAllUsersTestClass {

        private Users user1;
        private Users user2;

        @BeforeEach
        void setupUsers() {
            user1 = new Users();
            user1.setId(1L);
            user1.setFirstName("John");
            user1.setLastName("Doe");
            user1.setEmail("john.doe@example.com");
            user1.setMobileNumber("9876543210");

            user2 = new Users();
            user2.setId(2L);
            user2.setFirstName("Jane");
            user2.setLastName("Smith");
            user2.setEmail("jane.smith@example.com");
            user2.setMobileNumber("1234567890");
        }

        @Test
        @DisplayName("Should return list of users when database is not empty")
        void testGetAllUsersSuccess() {
            // Arrange
            List<Users> usersList = List.of(user1, user2);
            when(userRepo.findAll()).thenReturn(usersList);

            UserResponseDTO response1 = UserResponseDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .mobileNumber("9876543210")
                    .userRole(UserRole.USER)
                    .build();

            UserResponseDTO response2 = UserResponseDTO.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .mobileNumber("1234567890")
                    .userRole(UserRole.USER)
                    .build();

            when(userMappers.MpaUserToUserResponseDTO(user1)).thenReturn(response1);
            when(userMappers.MpaUserToUserResponseDTO(user2)).thenReturn(response2);

            // Act
            List<UserResponseDTO> actualResponses = userServiceImpl.getAllUsers();

            // Assert
            assertNotNull(actualResponses);
            assertEquals(2, actualResponses.size());
            assertEquals("John", actualResponses.get(0).getFirstName());
            assertEquals("Jane", actualResponses.get(1).getFirstName());

            // Verify interactions
            verify(userRepo, times(1)).findAll();
            verify(userMappers, times(1)).MpaUserToUserResponseDTO(user1);
            verify(userMappers, times(1)).MpaUserToUserResponseDTO(user2);
        }

        @Test
        @DisplayName("Should throw exception when database is empty")
        void testGetAllUsers_EmptyDatabase() {
            // Arrange
            when(userRepo.findAll()).thenReturn(List.of());

            // Act & Assert
            RuntimeException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                            () -> userServiceImpl.getAllUsers());

            assertEquals("Users Database is empty please try after some time", exception.getMessage());

            // Verify interactions
            verify(userRepo, times(1)).findAll();
            verify(userMappers, times(0)).MpaUserToUserResponseDTO(any());
        }
    }


}