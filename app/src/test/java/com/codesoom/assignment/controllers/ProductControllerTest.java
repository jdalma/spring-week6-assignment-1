package com.codesoom.assignment.controllers;

import com.codesoom.assignment.application.UserService;
import com.codesoom.assignment.dto.UserRegistrationData;
import com.codesoom.assignment.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(ProductController.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductController 테스트")
class ProductControllerTest {

    private final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9.neCsyNLzy3lQ4o2yliotWT06FwSGZagaHpKdAkjnGGw";
    private final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF1239.neCsyNLzy3lQ4o2yliotWT06FwSGZagaHpKdAkjnGGw";
    private final String CONTENT = "{\"name\":\"쥐돌이\",\"maker\":\"코드숨\",\"price\":1000}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService service;
    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {

    }

    long addUser(){
        UserRegistrationData user = UserRegistrationData.builder()
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();
        return service.registerUser(user).getId();
    }

    void deleteUser(Long userId){
        service.deleteUser(userId);
    }

    @Nested
    @DisplayName("create()")
    class Describe_Create{

        @Nested
        @DisplayName("인증 헤더가 존재하지 않는다면")
        class Context_NotExistedAuthenticationHeader{

            @Test
            @DisplayName("권한에 대한 승인이 거부되었다는 응답 코드를 반환한다.")
            void It_() throws Exception {
                mockMvc.perform(
                                post("/products")
                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(CONTENT)
                        )
                        .andExpect(status().isUnauthorized());
            }
        }

        @Nested
        @DisplayName("인증 헤더의 토큰이 유효하지 않거나 공백이라면")
        class Context_NullOrBlankAuthenticationHeader{

            @Test
            @DisplayName("권한에 대한 승인이 거부되었다는 응답 코드를 반환한다.")
            void It_ThrowException() throws Exception {
                mockMvc.perform(
                                post("/products")
                                        .header("Authorization" ," ")
                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(CONTENT)
                        )
                        .andExpect(status().isUnauthorized());

                mockMvc.perform(
                                post("/products")
                                        .header("Authorization" ,"Bearer " + INVALID_TOKEN)
                                        .accept(MediaType.APPLICATION_JSON_UTF8)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(CONTENT)
                        )
                        .andExpect(status().isUnauthorized());
            }
        }

        @Nested
        @DisplayName("정상적인 토큰이 담겨있다면")
        class Context_InvalidToken{

            @Nested
            @DisplayName("사용자가 존재하지 않는다면")
            class Context_NotExistedUser{

                @Test
                @DisplayName("사용자를 찾지 못 했다는 응답코드를 반환한다.")
                void It_SaveProduct() throws Exception {
                    mockMvc.perform(
                                    post("/products")
                                            .header("Authorization" , "Bearer " + VALID_TOKEN)
                                            .accept(MediaType.APPLICATION_JSON_UTF8)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(CONTENT)
                            )
                            .andExpect(status().isNotFound());
                }
            }

            @Nested
            @DisplayName("사용자가 존재한다면")
            class Context_{

                private Long userId;
                private String token;

                @BeforeEach
                void setUp() {
                    userId = addUser();
                    token = jwtUtil.encode(userId);
                }

                @AfterEach
                void tearDown() {
                    deleteUser(userId);
                }

                @Test
                @DisplayName("상품을 저장하고 자원을 생성했다는 응답코드를 반환한다.")
                void It_() throws Exception {
                    mockMvc.perform(
                                    post("/products")
                                            .header("Authorization" , "Bearer " + token)
                                            .accept(MediaType.APPLICATION_JSON_UTF8)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(CONTENT)
                            )
                            .andExpect(status().isCreated());
                }
            }
        }
    }
}
