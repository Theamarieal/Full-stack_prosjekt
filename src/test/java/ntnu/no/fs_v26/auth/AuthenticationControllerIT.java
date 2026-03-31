package ntnu.no.fs_v26.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterManagerCorrectly() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("manager@restaurant.no")
                .password("admin123")
                .role(Role.MANAGER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // verify in the database that the role was saved correctly
        Optional<User> user = userRepository.findByEmail("manager@restaurant.no");
        assertTrue(user.isPresent());
        assertEquals(Role.MANAGER, user.get().getRole());
    }

    @Test
    void shouldRegisterEmployeeCorrectly() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("waiter@restaurant.no")
                .password("waiter123")
                .role(Role.EMPLOYEE)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Optional<User> user = userRepository.findByEmail("waiter@restaurant.no");
        assertTrue(user.isPresent());
        assertEquals(Role.EMPLOYEE, user.get().getRole());
    }

    @Test
    void shouldLoginWithExistingCredentials() throws Exception {
        // save a user first
        RegisterRequest reg = RegisterRequest.builder()
                .email("chef@restaurant.no")
                .password("chef123")
                .role(Role.EMPLOYEE)
                .build();
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Login
        AuthenticationRequest login = AuthenticationRequest.builder()
                .email("chef@restaurant.no")
                .password("chef123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}