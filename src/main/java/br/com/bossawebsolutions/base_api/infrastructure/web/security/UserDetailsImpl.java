package br.com.bossawebsolutions.base_api.infrastructure.web.security;

import br.com.bossawebsolutions.base_api.model.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/**
 * Implementação personalizada da interface {@link UserDetails} do Spring Security.
 * Utilizada para fornecer informações sobre o usuário durante o processo de autenticação e autorização.
 */
@Getter
public class UserDetailsImpl implements UserDetails {

	private final String username;
	private final String password;
	private final Set<String> roles;
	private final Long id;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public UserDetailsImpl(AppUser appUser) {
		this.username = appUser.getUsername();
		this.password = appUser.getPassword();
		this.roles = appUser.getRoles();
		this.id = appUser.getId();
		this.createdAt = appUser.getCreatedAt();
		this.updatedAt = appUser.getUpdatedAt();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList(roles.toArray(new String[0]));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
