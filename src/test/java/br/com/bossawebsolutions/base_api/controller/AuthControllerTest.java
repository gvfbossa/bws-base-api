package br.com.bossawebsolutions.base_api.controller;

import br.com.bossawebsolutions.base_api.model.AppUser;
import br.com.bossawebsolutions.base_api.model.TestAppUser;
import br.com.bossawebsolutions.base_api.repository.AppUserRepository;
import br.com.bossawebsolutions.base_api.infrastructure.web.config.CustomObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserRepository appUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private AppUser mockAppUser;

    @BeforeEach
    public void setUp() {
        Map<String, Class<? extends AppUser>> appUserTypeMapping = Map.of(
                "TestAppUser", TestAppUser.class
        );
        CustomObjectMapper.registerAppUserImplementations(appUserTypeMapping);
        Set<String> roleSet = new HashSet<>();
        roleSet.add("ADMIN");
        mockAppUser = new TestAppUser(
                "user",
                passwordEncoder.encode(new String(java.util.Base64.getDecoder().decode("password"))),
                roleSet
        );

        Mockito.when(appUserRepository.findByUsername(anyString())).thenReturn(Optional.of(mockAppUser));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TestAppUser\", \"username\":\"user\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // Obtém o token do header da resposta
        String token = result.getResponse().getHeader("Authorization");
        System.out.println("Token recebido: " + token);

        // Verifique se o token não está vazio
        assertNotNull(token, "O token não deveria estar vazio");
    }

    @Test
    public void testLoginFailure() throws Exception {
        // Mocka falha no login: usuário não encontrado
        Mockito.when(appUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Realiza a chamada POST para login com credenciais incorretas
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"TestAppUser\", \"username\":\"user\", \"password\":\"password\"}"))
                .andExpect(status().isUnauthorized());
    }

}
