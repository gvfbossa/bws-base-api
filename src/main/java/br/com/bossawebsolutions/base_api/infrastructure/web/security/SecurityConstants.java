package br.com.bossawebsolutions.base_api.infrastructure.web.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Classe de constantes de segurança para a aplicação.
 * Contém configurações relacionadas à chave secreta para assinatura de tokens JWT, tempo de expiração e cabeçalhos de autorização.
 */
@Component
public class SecurityConstants {

	private final Environment environment;

	@Value("${secret.key}")
	private String secretKeyEnv;

	private static SecretKey SECRET_KEY;
	public static final long EXPIRATION_TIME = 86400000; // 1 dia
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	@Autowired
	public SecurityConstants(Environment environment) {
		this.environment = environment;
	}

	@PostConstruct
	public void init() {
		if (secretKeyEnv.contains("INVALID")) {
			if (!isTestOrBuildEnvironment()) {
				throw new IllegalStateException("SECRET_KEY must be defined in the environment for development or production.");
			}
		}
		SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyEnv));
	}

	private boolean isTestOrBuildEnvironment() {
		String[] activeProfiles = environment.getActiveProfiles();
		for (String profile : activeProfiles) {
			if ("test".equals(profile) || "build".equals(profile)) {
				return true;
			}
		}
		return false;
	}

	public static SecretKey getSecretKey() {
		return SECRET_KEY;
	}
}
