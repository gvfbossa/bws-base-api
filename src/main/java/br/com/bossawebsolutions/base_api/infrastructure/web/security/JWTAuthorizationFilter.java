	package br.com.bossawebsolutions.base_api.infrastructure.web.security;

	import io.jsonwebtoken.Jwts;
    import jakarta.servlet.http.Cookie;
    import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.security.authentication.AuthenticationManager;
	import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
	import org.springframework.security.core.authority.AuthorityUtils;
	import org.springframework.security.core.context.SecurityContextHolder;
	import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

	import jakarta.servlet.FilterChain;
	import jakarta.servlet.ServletException;
	import jakarta.servlet.http.HttpServletRequest;
	import jakarta.servlet.http.HttpServletResponse;
	import java.io.IOException;

	/**
	 * Filtro responsável pela autorização com base no token JWT.
	 * Extrai e valida o token JWT, configurando a autenticação no contexto de segurança.
	 */
	public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
		private static final Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

		/**
		 * @param authenticationManager o {@link AuthenticationManager} responsável pela autenticação
		 */
		public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
			super(authenticationManager);
		}

		/**
		 * Realiza a filtragem da requisição para verificar o token JWT.
		 * Se o token for válido, o processo de autenticação é configurado no contexto de segurança.
		 *
		 * @param request a requisição HTTP
		 * @param response a resposta HTTP
		 * @param chain a cadeia de filtros
		 * @throws IOException se ocorrer um erro de entrada/saída
		 * @throws ServletException se ocorrer um erro no processamento do filtro
		 */
		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {

            String token = resolveToken(request);

			if (token != null && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
				try {
					UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
					if (authentication != null) {
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				} catch (Exception e) {
					logger.error("Erro ao autenticar com o token JWT: {}", e.getMessage(), e);
                    SecurityContextHolder.clearContext();
                    clearCookie(response);
				}
			}
			chain.doFilter(request, response);
		}

		/**
		 * Recupera a autenticação a partir do token JWT.
		 *
		 * @param token o token JWT extraído da requisição
		 * @return um objeto {@link UsernamePasswordAuthenticationToken} ou {@code null} se a autenticação falhar
		 */
		private UsernamePasswordAuthenticationToken getAuthentication(String token) {
			try {
				String username = Jwts.parser()
					.verifyWith(SecurityConstants.getSecretKey())
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();

				if (username != null) {
					return new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.NO_AUTHORITIES);
				}
			} catch (Exception e) {
				logger.error("Erro ao processar o token JWT: {}", e.getMessage(), e);
			}
			return null;
		}

        /**
         * Extrai o token JWT do cookie OU do header (pra compatibilidade com mobile/postman)
         */
        private String resolveToken(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("access_token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
            if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                return header.substring(SecurityConstants.TOKEN_PREFIX.length());
            }
            return null;
        }

        /**
         * Limpa o cookie se o token estiver expirado/inválido
         */
        private void clearCookie(HttpServletResponse response) {
            Cookie cookie = new Cookie("access_token", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

	}
