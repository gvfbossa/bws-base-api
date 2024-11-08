package br.com.bossawebsolutions.base_api.infrastructure.web.config;

import br.com.bossawebsolutions.base_api.model.AppUser;
import br.com.bossawebsolutions.base_api.repository.AppUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("build")
public class FakeAppUserRepository implements AppUserRepository {
    @Override
    public Optional<AppUser> findByUsername(String username) {
        return Optional.empty();
    }
}
