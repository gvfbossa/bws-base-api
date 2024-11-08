# API Base de Autenticação e JWT

### Este projeto fornece uma API genérica para autenticação de usuários, geração de tokens JWT e validação de credenciais. Ele foi projetado para ser facilmente integrado a outros projetos por meio de um arquivo .jar. A API é altamente configurável e permite que você forneça sua própria implementação de usuários.
## Funcionalidades

###    Autenticação de usuários: Realiza a validação de credenciais de usuários.
    Geração de tokens JWT: Gera e valida tokens JWT para autenticação segura.
    Sistema de permissões: Suporte a diferentes papéis e permissões de usuários.
    API genérica: Totalmente configurável e sem dependências de implementação específica para a interface AppUser.

### Integração com Outros Projetos

#### Esta API foi projetada para ser agnóstica quanto à implementação concreta da interface AppUser. Ou seja, você pode facilmente criar sua própria implementação da interface AppUser em seu projeto e integrar com a API.
### Requisitos

    Java 8 ou superior.
    Spring Boot 3.x
    Dependência do spring-security para autenticação JWT
    Dependência do jackson para serialização/deserialização de objetos.

## Como Integrar

Adicionar dependência no seu projeto

Para utilizar esta API, primeiro gere o arquivo .jar à partir do comando `mvn clean package`.
Segundo, adicione a dependência no arquivo pom.xml de seu projeto Maven.
    
    <dependency>
        <groupId>br.com.bossawebsolutions</groupId>
        <artifactId>bws-base-api</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/bws-base-api-0.0.1-SNAPSHOT.jar</systemPath>
    </dependency>

## Configuração da Variável de Ambiente

Esta API só irá funcionar se houver uma Variável de Ambiente configurada, tanto para os ambientes de dev como prod.
Ela deve ter a chave `SECRET_KEY`

## Implementação de AppUser

A interface AppUser é usada para representar os usuários do sistema. Você precisa fornecer uma implementação concreta dessa interface em seu projeto, pois a API não assume uma implementação específica.

Exemplo de implementação:

    package com.seuprojeto.model;
    import br.com.bossawebsolutions.base_api.model.AppUser;

    public class MyAppUser implements AppUser {
        private String username;
        private String password;
        private Set<String> roles;

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public Set<String> getRoles() {
            return roles;
        }

        // Outros métodos de acordo com suas necessidades
    }

A role é um Set<String> que deve ser configurado no lado consumidor da API. Você pode mapear os papéis conforme necessário em seu projeto, como "ADMIN", "USER", etc.

Caso sua implementação exija algo mais específico, como informações extras do usuário, adicione conforme necessário.

## Implementação do Repository

A API não assume um repositório específico, então você também deve fornecer a implementação do repositório para a sua interface AppUser. Aqui está um exemplo de como criar um repositório para a implementação MyAppUser.

### Exemplo de repositório:

    package com.seuprojeto.repository;
    
    import com.seuprojeto.model.MyAppUser;
    import org.springframework.data.jpa.repository.JpaRepository;

    public interface MyAppUserRepository extends JpaRepository<MyAppUser, Integer> {
        MyAppUser findByUsername(String username);
    }

## Configuração do ObjectMapper

A API usa a biblioteca Jackson para serializar e desserializar objetos JSON. Como a interface AppUser é genérica e a implementação pode variar, você deve mapear a interface para sua implementação concreta durante a configuração da API.

### Configuração do ObjectMapper:

Em seu projeto, registre a implementação concreta de AppUser que será usada. Isso garante que a API sabe como desserializar objetos AppUser.

    import br.com.bossawebsolutions.base_api.infrastructure.web.config.CustomObjectMapper;
    import com.seuprojeto.model.MyAppUser;
    
    public class ApplicationConfig {
        public static void main(String[] args) {
            // Cria o mapeamento entre o tipo de AppUser e a implementação concreta
            Map<String, Class<? extends AppUser>> appUserTypeMapping = new HashMap<>();
            appUserTypeMapping.put("myAppUser", MyAppUser.class);

            // Registra a implementação de AppUser
            CustomObjectMapper.registerAppUserImplementations(appUserTypeMapping);
            
            // Agora você pode usar o ObjectMapper configurado na sua aplicação
        }
    }

### Módulo de Autenticação e JWT

A API vem com um módulo de autenticação JWT que você pode usar diretamente ou estender conforme necessário.

    Autenticação de usuário: A função de autenticação espera um JSON contendo username e password.
    Geração de JWT: Após a autenticação bem-sucedida, um token JWT é gerado e retornado para o cliente.

Exemplo de endpoint de login (Spring Boot):

    @RestController
    public class AuthenticationController {

        @Autowired
        private AuthenticationService authenticationService;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
            String token = authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new JwtResponse(token));
        }
    }

Onde JwtResponse contém o token JWT gerado, e LoginRequest é uma classe simples contendo os dados do login.

### Personalizando a API

A API pode ser facilmente personalizada para se adaptar às necessidades do seu projeto. Você pode modificar ou estender os seguintes componentes:

    Validação de usuário: A API valida a credencial de AppUser. Você pode substituir a lógica de autenticação ou fazer a integração com seu próprio repositório de dados.
    Geração de JWT: A geração do token pode ser customizada, caso você queira adicionar informações extras ao token ou mudar a forma de validá-lo.

### Testando a API

Simulação de Usuário com Mocks: Quando integrar a API, você pode simular o comportamento da interface AppUser utilizando frameworks de testes como Mockito. Isso permite que você teste a autenticação e a geração de tokens sem precisar de uma implementação real de AppUser.

Exemplo de Teste Unitário com Mockito:

    @Test
    void testAuthenticate() throws Exception {
        String json = "{\"username\":\"user\", \"password\":\"password123\"}";

        // Mocka o comportamento do AppUser
        AppUser mockUser = mock(AppUser.class);
        when(mockUser.getUsername()).thenReturn("user");
        when(mockUser.getPassword()).thenReturn("password123");

        AuthenticationService authService = new AuthenticationService();
        String token = authService.authenticate(mockUser.getUsername(), mockUser.getPassword());

        // Verifique se o token foi gerado corretamente
        assertNotNull(token);
    }

## Conclusão

Esse README serve como um guia completo para quem quiser integrar sua API a outro projeto. Ele oferece exemplos de como configurar a API, como mapear a interface AppUser para a implementação concreta de um projeto específico, e como realizar testes com mocks. Isso torna a API mais flexível e fácil de ser utilizada em qualquer contexto.