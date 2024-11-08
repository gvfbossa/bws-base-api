package br.com.bossawebsolutions.base_api.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Interface que define os métodos essenciais para um usuário no sistema.
 * Esta interface deve ser implementada por uma classe que represente um usuário do sistema.
 */
public interface AppUser {
	Long getId();
	String getUsername();
	String getPassword();
	Set<String> getRoles();
	LocalDateTime getCreatedAt();
	LocalDateTime getUpdatedAt();
}
