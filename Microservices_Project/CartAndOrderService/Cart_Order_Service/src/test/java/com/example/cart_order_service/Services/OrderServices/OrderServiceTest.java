package com.example.cart_order_service.Services.OrderServices;

import com.example.cart_order_service.DTOs.OrdersDto.OrderResponseDTO;
import com.example.cart_order_service.Entites.CartItem;
import com.example.cart_order_service.Entites.Order;
import com.example.cart_order_service.Entites.OrderStatus;
import com.example.cart_order_service.ExceptionHandlers.OrdersExceptionHandling.OrderUserNotFoundException;
import com.example.cart_order_service.Mappers.OrdersMapper.OrderMapperClass;
import com.example.cart_order_service.Repositories.CartItemRepo;
import com.example.cart_order_service.Repositories.OrderRepo;
import com.example.cart_order_service.Services.CartItemServices.CartItemService;
import com.example.cart_order_service.Services.OrderServices.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceTest")
class OrderServiceTest {

    @Mock private CartItemRepo cartItemRepo;
    @Mock private OrderRepo orderRepo;
    @Mock private CartItemService cartItemService;
    @Mock private OrderMapperClass orderMapperClass;
    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private OrderService orderService;

    private CartItem cartItem;
    private Order order;
    private OrderResponseDTO orderResponseDTO;

    @BeforeEach
    void setup() {
        cartItem = new CartItem();
        cartItem.setUserId("1");
        cartItem.setProductId("101");
        cartItem.setQuantity(2);
        cartItem.setPrice(BigDecimal.valueOf(500));

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setTotalAmount(BigDecimal.valueOf(1000));
        order.setStatus(OrderStatus.CONFIRMED);

        orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(1L);
        orderResponseDTO.setUserId("1");
        orderResponseDTO.setTotalAmount(BigDecimal.valueOf(1000));
        orderResponseDTO.setStatus(OrderStatus.CONFIRMED);
    }

    @Nested
    @DisplayName("createOrderTest")
    class CreateOrderTest {
        @Test void testCreateOrderSuccess() {
            when(cartItemRepo.findByUserId("1")).thenReturn(List.of(cartItem));
            when(orderRepo.save(any(Order.class))).thenReturn(order);
            when(cartItemService.deleteCartItemByUser("1")).thenReturn(true);
            when(orderMapperClass.MapOrderToOrderResponseDTO(order)).thenReturn(orderResponseDTO);

            OrderResponseDTO actual = orderService.createOrder(1L);

            assertEquals(BigDecimal.valueOf(1000), actual.getTotalAmount());
            verify(orderRepo, times(1)).save(any(Order.class));
            verify(streamBridge, times(1)).send(eq("createOrder-out-0"), any());
            verify(cartItemService, times(1)).deleteCartItemByUser("1");
        }

        @Test void testCreateOrderCartEmpty() {
            when(cartItemRepo.findByUserId("1")).thenReturn(List.of());
            assertThrows(OrderUserNotFoundException.class,
                    () -> orderService.createOrder(1L));
        }

        @Test
        void testCreateOrderCartNotDeleted() {
            when(cartItemRepo.findByUserId("1")).thenReturn(List.of(cartItem));
            when(orderRepo.save(any(Order.class))).thenReturn(order);
            when(cartItemService.deleteCartItemByUser("1")).thenReturn(false);
            assertThrows(OrderUserNotFoundException.class,
                    () -> orderService.createOrder(1L));
        }
    }
}
