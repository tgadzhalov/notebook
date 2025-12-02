package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;


    @Test
    void registerUser_whenEmailIsTaken_ThrowIllegalArgumentException() {
        String email = "random@email.com";
        
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password("12312312")
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registerDto);
        });
        
        assertEquals("Email already exists: " + email, exception.getMessage());
    }

    @Test
    void registerUser_whenValidData_thenSaveUser() {
        String email = "newuser@example.com";
        String password = "12312312";
        String encodedPassword = "encodedPassword123";
        
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password(password)
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.registerUser(registerDto);
        
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }
}