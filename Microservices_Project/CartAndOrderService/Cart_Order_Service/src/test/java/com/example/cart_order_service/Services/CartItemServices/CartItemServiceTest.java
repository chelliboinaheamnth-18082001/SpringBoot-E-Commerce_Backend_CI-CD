package com.example.cart_order_service.Services.CartItemServices;

import com.example.cart_order_service.DTOs.CartItemDTOs.CartItemRequestDTO;
import com.example.cart_order_service.DTOs.CartItemDTOs.CartItemsResponseDTO;
import com.example.cart_order_service.DTOs.ProductsDTOs.ProductResponseDTO;
import com.example.cart_order_service.DTOs.UserDTOs.UserResponseDTO;
import com.example.cart_order_service.Entites.CartItem;
import com.example.cart_order_service.ExceptionHandlers.CartExceptionHandler.CartItemNotFoundException;
import com.example.cart_order_service.ExceptionHandlers.ProductRelatedExceptions.ProductNotFoundException;
import com.example.cart_order_service.ExceptionHandlers.ProductRelatedExceptions.ProductOutOfStockException;
import com.example.cart_order_service.ExceptionHandlers.UserrelatedException.UserNotFoundException;
import com.example.cart_order_service.Inservice_Commnication_Client.ProductServiceClientInterface;
import com.example.cart_order_service.Inservice_Commnication_Client.UserServiceClientInterface;
import com.example.cart_order_service.Mappers.CartItemsMappers.CartItemMapper;
import com.example.cart_order_service.Repositories.CartItemRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartItemServiceTest")
class CartItemServiceTest {

    @Mock
    private CartItemRepo cartItemRepo;
    @Mock private ProductServiceClientInterface productServiceClient;
    @Mock private UserServiceClientInterface userServiceClientInterface;
    @Mock private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartItemService cartItemService;

    private CartItemRequestDTO requestDTO;
    private ProductResponseDTO productResponseDTO;
    private UserResponseDTO userResponseDTO;
    private CartItem cartItem;
    private CartItemsResponseDTO cartItemsResponseDTO;

    @BeforeEach
    void setup() {
        requestDTO = new CartItemRequestDTO();
        requestDTO.setProductId("101");
        requestDTO.setQuantity(2);

        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setQuantity(10);
        productResponseDTO.setPrice(BigDecimal.valueOf(500));

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setFirstName("John");

        cartItem = new CartItem();
        cartItem.setUserId("1");
        cartItem.setProductId("101");
        cartItem.setQuantity(2);
        cartItem.setPrice(BigDecimal.valueOf(500));

        cartItemsResponseDTO = new CartItemsResponseDTO();
        cartItemsResponseDTO.setUserId("1");
        cartItemsResponseDTO.setProductid("101");
        cartItemsResponseDTO.setQuantity(2);
        cartItemsResponseDTO.setPrice(BigDecimal.valueOf(500));
    }


    @Nested
    @DisplayName("createCartItemTest")
    class CreateCartItemTest {
        @Test
        void testCreateCartItemSuccess() {
            when(userServiceClientInterface.getUserById(1L)).thenReturn(userResponseDTO);
            when(productServiceClient.getProductById(101L)).thenReturn(productResponseDTO);
            when(cartItemRepo.findByUserIdAndProductId("1", "101")).thenReturn(Optional.empty());
            when(cartItemRepo.save(any(CartItem.class))).thenReturn(cartItem);
            when(cartItemMapper.MapCartItemToCartItemsResponseDTO(cartItem, 1L, "101"))
                    .thenReturn(cartItemsResponseDTO);

            CartItemsResponseDTO actual = cartItemService.createCartItem(1L, requestDTO);

            assertEquals("101", actual.getProductid());
            verify(cartItemRepo, times(1)).save(any(CartItem.class));
        }

        @Test void testUserNotFound() {
            when(userServiceClientInterface.getUserById(1L))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
            assertThrows(UserNotFoundException.class,
                    () -> cartItemService.createCartItem(1L, requestDTO));
        }

        @Test void testInvalidQuantity() {
            requestDTO.setQuantity(0);
            assertThrows(IllegalArgumentException.class,
                    () -> cartItemService.createCartItem(1L, requestDTO));
        }

        @Test void testProductNotFound() {
            when(userServiceClientInterface.getUserById(1L)).thenReturn(userResponseDTO);
            when(productServiceClient.getProductById(101L))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
            assertThrows(ProductNotFoundException.class,
                    () -> cartItemService.createCartItem(1L, requestDTO));
        }

        @Test void testProductOutOfStock() {
            productResponseDTO.setQuantity(1);
            when(userServiceClientInterface.getUserById(1L)).thenReturn(userResponseDTO);
            when(productServiceClient.getProductById(101L)).thenReturn(productResponseDTO);
            when(cartItemRepo.findByUserIdAndProductId("1", "101")).thenReturn(Optional.empty());
            assertThrows(ProductOutOfStockException.class,
                    () -> cartItemService.createCartItem(1L, requestDTO));
        }

        @Test
        @DisplayName("Should update existing cart item when present")
        void testUpdateExistingCartItem() {
            when(userServiceClientInterface.getUserById(1L)).thenReturn(userResponseDTO);
            when(productServiceClient.getProductById(101L)).thenReturn(productResponseDTO);

            // Existing cart item with quantity 2
            cartItem.setQuantity(2);
            when(cartItemRepo.findByUserIdAndProductId("1", "101")).thenReturn(Optional.of(cartItem));

            // Simulate updated quantity (2 existing + 2 new = 4)
            CartItem updatedCartItem = new CartItem();
            updatedCartItem.setUserId("1");
            updatedCartItem.setProductId("101");
            updatedCartItem.setQuantity(4);
            updatedCartItem.setPrice(BigDecimal.valueOf(500));

            when(cartItemRepo.save(any(CartItem.class))).thenReturn(updatedCartItem);
            when(cartItemMapper.MapCartItemToCartItemsResponseDTO(updatedCartItem, 1L, "101"))
                    .thenReturn(cartItemsResponseDTO);

            // Adjust expected DTO to reflect updated quantity
            cartItemsResponseDTO.setQuantity(4);

            CartItemsResponseDTO actual = cartItemService.createCartItem(1L, requestDTO);

            assertEquals(4, actual.getQuantity()); // ✅ now matches
            verify(cartItemRepo, times(1)).save(any(CartItem.class));
        }


        @Test
        @DisplayName("Should propagate infra error when product service fails with non-404")
        void testInfraErrorPropagation() {
            when(userServiceClientInterface.getUserById(1L)).thenReturn(userResponseDTO);
            when(productServiceClient.getProductById(101L))
                    .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThrows(ResponseStatusException.class,
                    () -> cartItemService.createCartItem(1L, requestDTO));
        }

    }

    @Nested
    @DisplayName("getAllCartItemsTest")
    class GetAllCartItemsTest {
        @Test void testGetAllCartItemsSuccess() {
            when(cartItemRepo.findByUserId("1")).thenReturn(List.of(cartItem));
            when(cartItemMapper.MapCartItemToCartItemsResponseDTO(cartItem, 1L, "101"))
                    .thenReturn(cartItemsResponseDTO);
            List<CartItemsResponseDTO> result = cartItemService.getAllCartItems(1L);
            assertEquals(1, result.size());
        }

        @Test void testCartItemsNotFound() {
            when(cartItemRepo.findByUserId("1")).thenReturn(List.of());
            assertThrows(CartItemNotFoundException.class,
                    () -> cartItemService.getAllCartItems(1L));
        }
    }

    @Nested
    @DisplayName("deleteCartItemsTest")
    class DeleteCartItemsTest {
        @Test void testDeleteCartItems() {
            cartItemService.deleteCartItems(1L);
            verify(cartItemRepo, times(1)).deleteByUserId("1");
        }
    }

    @Nested
    @DisplayName("deleteCartItemByUserTest")
    class DeleteCartItemByUserTest {
        @Test void testDeleteCartItemByUser() {
            boolean result = cartItemService.deleteCartItemByUser("1");
            assertTrue(result);
            verify(cartItemRepo, times(1)).deleteByUserId("1");
        }
    }

    @Nested
    @DisplayName("createCartItemFallbackTest")
    class CreateCartItemFallbackTest {
        @Test
        void testFallbackProductNotFound() {
            assertThrows(ProductNotFoundException.class,
                    () -> cartItemService.createCartItemFallback(1L, requestDTO,
                            new ProductNotFoundException("Product Not Found")));
        }

        @Test void testFallbackInfraFailure() {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> cartItemService.createCartItemFallback(1L, requestDTO,
                            new RuntimeException("Infra error")));
            assertEquals("Product Service is currently unavailable. Please try again later.", ex.getMessage());
        }
        @Test
        @DisplayName("Should rethrow UserNotFoundException")
        void testFallbackUserNotFound() {
            assertThrows(UserNotFoundException.class,
                    () -> cartItemService.createCartItemFallback(1L, requestDTO,
                            new UserNotFoundException("User Not Found")));
        }

        @Test
        @DisplayName("Should rethrow ProductOutOfStockException")
        void testFallbackProductOutOfStock() {
            assertThrows(ProductOutOfStockException.class,
                    () -> cartItemService.createCartItemFallback(1L, requestDTO,
                            new ProductOutOfStockException("Out of stock")));
        }

    }
}
