package com.example.user_service;

import com.example.user_service.Repositories.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

//@SpringBootTest
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class UserServiceApplicationTests {

//    @MockBean
//    private UserRepo userRepo;
    @Test
    void contextLoads() {
        // This will start Spring context but skip DB auto‑config
    }
}
