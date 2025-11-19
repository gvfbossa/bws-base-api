package br.com.bossawebsolutions.base_api.infrastructure.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import  org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

/**
 * Configuração de segurança do Spring Security para autenticação e autorização JWT.
 */
@Configuration
public class WebSecurityConfig {
	private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

	private final UserDetailsServiceImpl userDetailsService;

    @Value("${open.endpoints.list:}")
    List<String> openEndpointsList;

    @Value("${allowed.origins.list:}")
    List<String> allowedOriginsList;

	/**
	 * @param userDetailsService implementação personalizada para carregar os detalhes do usuário.
	 */
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

	/**
	 * Bean para configurar o {@link AuthenticationManager} com o serviço de detalhes do usuário e o codificador de senha.
	 *
	 * @param http objeto {@link HttpSecurity} utilizado para configurar segurança da aplicação.
	 * @return o {@link AuthenticationManager} configurado.
	 * @throws Exception se houver erro ao configurar o AuthenticationManager.
	 */
    @Bean
	AuthenticationManager authenticationManager(HttpSecurity http)
			throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		return authenticationManagerBuilder.build();
	}

	/**
	 * Bean para criar o codificador de senha {@link BCryptPasswordEncoder}.
	 *
	 * @return o codificador de senha {@link BCryptPasswordEncoder}.
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Bean para configurar o {@link SecurityFilterChain} que define as regras de segurança HTTP.
	 *
	 * Configura a política de CORS, desativa o CSRF, define as permissões para a URL "/login",
	 * e adiciona os filtros de autenticação e autorização JWT.
	 *
	 * @param http objeto {@link HttpSecurity} utilizado para configurar segurança da aplicação.
	 * @return o {@link SecurityFilterChain} configurado.
	 * @throws Exception se houver erro ao configurar a segurança HTTP.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
                    config.setAllowCredentials(true);
                    config.addExposedHeader("Set-Cookie");
                    config.setAllowedOrigins(allowedOriginsList);
                    return config;
                }))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(openEndpointsList.toArray(new String[0])).permitAll()
						.anyRequest().authenticated()
				)
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new JWTAuthenticationFilter(authenticationManager(http)), UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(new JWTAuthorizationFilter(authenticationManager(http)), JWTAuthenticationFilter.class)
				.httpBasic(Customizer.withDefaults())
				.build();
	}

}
