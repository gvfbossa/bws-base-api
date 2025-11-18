package br.com.bossawebsolutions.base_api.infrastructure.web.security;

import br.com.bossawebsolutions.base_api.infrastructure.web.config.CustomObjectMapper;
import br.com.bossawebsolutions.base_api.model.AppUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Realiza a validação de credenciais e gera um token JWT no caso de autenticação bem-sucedida.
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

	private final AuthenticationManager authenticationManager;

	/**
	 * @param authenticationManager o {@link AuthenticationManager} responsável pela autenticação.
	 */
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
		setFilterProcessesUrl("/login");
	}

	/**
	 * Tenta autenticar o usuário com base nas credenciais fornecidas na requisição.
	 *
	 * @param request a requisição HTTP contendo as credenciais do usuário
	 * @param response a resposta HTTP
	 * @return um objeto {@link Authentication} com as credenciais validadas
	 * @throws AuthenticationException se ocorrer um erro durante a autenticação
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		
		try {
			ObjectMapper mapper = CustomObjectMapper.getInstance();
			AppUser appUser = mapper.readValue(request.getInputStream(), AppUser.class);
            String decodedPassword = new String(java.util.Base64.getDecoder().decode(appUser.getPassword()));
			UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(appUser.getUsername(), decodedPassword);
			return authenticationManager.authenticate(upat);
		
		} catch (IOException e) {
			logger.error("Erro ao tentar autenticar o usuário: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executa após a autenticação bem-sucedida, gerando o token JWT e adicionando-o aos cabeçalhos da resposta.
	 *
	 * @param request a requisição HTTP
	 * @param response a resposta HTTP
	 * @param chain a cadeia de filtros
	 * @param authResult o resultado da autenticação
	 * @throws IOException se ocorrer um erro ao escrever a resposta
	 * @throws ServletException se ocorrer um erro na execução do filtro
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		try {
			UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();

			Set<String> roles = userDetails.getRoles();
			String rolesClaim = roles != null ? String.join(",", roles) : "DEFAULT_ROLE";

			String jwtToken = Jwts.builder()
					.subject(userDetails.getUsername())
					.expiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
					.claim("username", userDetails.getUsername())
					.claim("roles", rolesClaim)
					.claim("createdAt", userDetails.getCreatedAt() != null ? userDetails.getCreatedAt().toString() : LocalDateTime.now().toString())
					.claim("updatedAt", userDetails.getUpdatedAt() != null ? userDetails.getUpdatedAt().toString() : LocalDateTime.now().toString())
					.claim("id", userDetails.getId())
					.claim("iat", new Date())
					.claim("jti", UUID.randomUUID().toString())
					.signWith(SecurityConstants.getSecretKey(), Jwts.SIG.HS512)
					.compact();

            Cookie accessTokenCookie = new Cookie("access_token", jwtToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure("https".equals(request.getScheme()));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (SecurityConstants.EXPIRATION_TIME / 1000));
            accessTokenCookie.setAttribute("SameSite", "Strict");

            response.addCookie(accessTokenCookie);
		} catch (Exception e) {
			logger.error("Erro ao gerar o token JWT: {}", e.getMessage(), e);
			throw new ServletException("Erro ao gerar o token JWT", e);
		}
	}
}
