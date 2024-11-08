package br.com.bossawebsolutions.base_api.infrastructure.web.security;

import br.com.bossawebsolutions.base_api.model.AppUser;
import br.com.bossawebsolutions.base_api.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementação personalizada do serviço {@link UserDetailsService} do Spring Security.
 * Responsável por carregar as informações do usuário para o processo de autenticação baseado no nome de usuário.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	private final AppUserRepository appUserRepository;

	/**
	 * @param appUserRepository o repositório que fornece acesso aos dados do {@link AppUser}.
	 */
	@Autowired
	public UserDetailsServiceImpl(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	/**
	 * Carrega as informações do usuário baseado no nome de usuário fornecido.
	 * Se o usuário não for encontrado no banco de dados, lança uma exceção {@link UsernameNotFoundException}.
	 *
	 * @param username o nome de usuário que está sendo utilizado para a autenticação.
	 * @return um objeto {@link UserDetails} contendo as informações do usuário.
	 * @throws UsernameNotFoundException se o usuário não for encontrado no banco de dados.
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<AppUser> appUser = appUserRepository.findByUsername(username);
		
		if (appUser.isEmpty()) {
			logger.error("Usuário não encontrado: {}", username);
			throw new UsernameNotFoundException(username);
		}
		
		return new UserDetailsImpl(appUser.get());
	}
}
