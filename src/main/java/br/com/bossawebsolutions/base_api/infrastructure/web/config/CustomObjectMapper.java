package br.com.bossawebsolutions.base_api.infrastructure.web.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import br.com.bossawebsolutions.base_api.infrastructure.web.security.AppUserDeserializer;
import br.com.bossawebsolutions.base_api.model.AppUser;

import java.util.Map;

/**
 * A classe {@code CustomObjectMapper} fornece uma instância singleton de {@link ObjectMapper} configurada para serialização e
 * desserialização de objetos JSON. Ela permite registrar implementações customizadas de {@link AppUser}.
 */
public class CustomObjectMapper {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
    }

    /**
     * Registra as implementações de {@link AppUser} para que o {@link ObjectMapper} saiba como desserializar diferentes tipos de usuários.
     *
     * @param appUserTypeMapping um mapa associando o tipo de usuário (chave) à sua implementação concreta (valor).
     *                            Exemplo: {"myAppUser" -> MyAppUser.class}
     */
    public static void registerAppUserImplementations(Map<String, Class<? extends AppUser>> appUserTypeMapping) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(AppUser.class, new AppUserDeserializer(appUserTypeMapping));
        objectMapper.registerModule(module);
    }

    /**
     * Obtém a instância singleton do {@link ObjectMapper}.
     *
     * @return a instância configurada do {@link ObjectMapper}.
     */
    public static ObjectMapper getInstance() {
        return objectMapper;
    }
}
