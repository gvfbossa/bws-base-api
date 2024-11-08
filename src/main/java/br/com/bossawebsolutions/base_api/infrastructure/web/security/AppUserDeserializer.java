package br.com.bossawebsolutions.base_api.infrastructure.web.security;

import br.com.bossawebsolutions.base_api.model.AppUser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Permite a desserialização de diferentes implementações de {@link AppUser} com base no campo "type".
 */
public class AppUserDeserializer extends JsonDeserializer<AppUser> {
    private static final Logger logger = LoggerFactory.getLogger(AppUserDeserializer.class);

    private final Map<String, Class<? extends AppUser>> appUserTypeMapping;

    /**
     *
     * @param appUserTypeMapping mapeamento de tipos para as classes de implementação de {@link AppUser}.
     */
    public AppUserDeserializer(Map<String, Class<? extends AppUser>> appUserTypeMapping) {
        this.appUserTypeMapping = appUserTypeMapping;
    }

    /**
     * Desserializa um objeto {@link AppUser} a partir de um parser JSON.
     *
     * @param p o parser JSON
     * @param ctxt o contexto de desserialização
     * @return a instância da implementação correspondente de {@link AppUser}
     * @throws IOException se ocorrer um erro durante a desserialização
     */
    @Override
    public AppUser deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);
        String type = node.get("type").asText();

        Class<? extends AppUser> appUserClass = appUserTypeMapping.get(type);
        if (appUserClass == null) {
            logger.error("Erro durante a desserialização: tipo não reconhecido '{}'", type);
            throw new IOException("Tipo não reconhecido: " + type);
        }
        node.remove("type");
        return p.getCodec().treeToValue(node, appUserClass);
    }
}
