package ntnu.no.fs_v26.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnForbiddenWhenAccessingProtectedResourceWithoutToken() throws Exception {
        // we will try to reach a route that isn't /api/v1/auth/**
        mockMvc.perform(get("/api/v1/protected-data"))
                .andExpect(status().isForbidden()); // should give 403
    }

    @Test
    void shouldAllowAccessToAuthEndpoints() throws Exception {
        // login/signup should be open
        mockMvc.perform(get("/api/v1/auth/test"))
                .andExpect(status().isNotFound()); // 404 is good here, it means we go through the security (but the route doesn't exist yet)
    }
}