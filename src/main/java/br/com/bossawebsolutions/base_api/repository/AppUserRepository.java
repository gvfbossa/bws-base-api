package br.com.bossawebsolutions.base_api.repository;

import br.com.bossawebsolutions.base_api.model.AppUser;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Interface de repositório para manipulação de dados de usuários (AppUser).
 * O Repository que implementar a interface deve estender JpaRepository para facilitar operações de persistência e recuperação de usuários no banco de dados.
 */
@NoRepositoryBean
public interface AppUserRepository {

	Optional<AppUser> findByUsername(String username);
}
