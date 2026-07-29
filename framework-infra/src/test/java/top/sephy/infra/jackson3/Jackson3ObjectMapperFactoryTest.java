/*
 * Copyright 2022-2026 sephy.top
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.sephy.infra.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.Data;
import tools.jackson.databind.ObjectMapper;
import top.sephy.infra.jackson.annotation.EncryptedField;
import top.sephy.infra.utils.JacksonUtils;

class Jackson3ObjectMapperFactoryTest {

    private final ObjectMapper mapper = Jackson3ObjectMapperFactory.newDefaultObjectMapper();

    @Test
    void shouldUseExtractedDateSerializersAndDeserializers() {
        DateFixture fixture = new DateFixture();
        fixture.setLocalDate(LocalDate.of(1970, 1, 1));
        fixture.setLocalDateTime(LocalDateTime.of(1970, 1, 1, 0, 0));

        String json = mapper.writeValueAsString(fixture);
        DateFixture result = mapper.readValue("{\"localDate\":0,\"localDateTime\":0}", DateFixture.class);

        assertTrue(mapper.readTree(json).get("localDate").isNumber());
        assertEquals(LocalDate.of(1970, 1, 1), result.getLocalDate());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()),
            result.getLocalDateTime());
    }

    @Test
    void shouldKeepDefaultAndIncludeNullPropertyPolicies() {
        NullFixture fixture = new NullFixture();

        Map<String, Object> defaultValues = mapper.convertValue(fixture, Map.class);
        Map<String, Object> includeNullValues = Jackson3ObjectMapperFactory.newDefaultObjectMapperIncludeNull()
            .convertValue(fixture, Map.class);

        assertFalse(defaultValues.containsKey("nullable"));
        assertTrue(includeNullValues.containsKey("nullable"));
    }

    @Test
    void shouldEnableEncryptedFieldsOnlyForKeyAwareMapper() {
        EncryptedFixture fixture = new EncryptedFixture();
        fixture.setSecret("secret-value");
        fixture.setOrdinary("ordinary-value");

        ObjectMapper encryptedMapper = Jackson3ObjectMapperFactory.newDefaultObjectMapper("default-aes-key");
        String encryptedJson = JacksonUtils.toJson(encryptedMapper, fixture);

        assertTrue(encryptedMapper.readTree(encryptedJson).get("secret").isObject());
        assertFalse(encryptedJson.contains("secret-value"));
        EncryptedFixture decrypted = encryptedMapper.readValue(encryptedJson, EncryptedFixture.class);
        assertEquals("secret-value", decrypted.getSecret());
        assertEquals("ordinary-value", decrypted.getOrdinary());
    }

    @Test
    void shouldKeepAnnotatedFieldsPlainWhenEncryptionIsDisabled() {
        EncryptedFixture fixture = new EncryptedFixture();
        fixture.setSecret("secret-value");
        fixture.setOrdinary("ordinary-value");

        ObjectMapper plainMapper = JacksonUtils.newDefaultObjectMapperWithoutEncryption();
        String json = JacksonUtils.toJson(plainMapper, fixture);
        Map<String, Object> values = JacksonUtils.convertToMap(plainMapper, fixture);

        assertEquals("secret-value", plainMapper.readTree(json).get("secret").asString());
        assertEquals("secret-value", values.get("secret"));
        EncryptedFixture result = plainMapper.readValue(json, EncryptedFixture.class);
        assertEquals("secret-value", result.getSecret());
    }

    @Data
    private static class DateFixture {

        private LocalDate localDate;

        private LocalDateTime localDateTime;
    }

    @Data
    private static class NullFixture {

        private String nullable;
    }

    @Data
    private static class EncryptedFixture {

        @EncryptedField
        private String secret;

        private String ordinary;
    }
}
