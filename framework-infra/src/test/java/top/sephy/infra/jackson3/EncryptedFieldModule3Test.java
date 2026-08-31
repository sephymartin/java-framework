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

import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.Data;
import tools.jackson.databind.json.JsonMapper;
import top.sephy.infra.jackson.annotation.EncryptedField;
import top.sephy.infra.utils.AESUtils;

class EncryptedFieldModule3Test {

    private final JsonMapper mapper = JsonMapper.builder()
        .addModule(new EncryptedFieldModule3("default-aes-key"))
        .build();

    /**
     * Test scenario: a globally registered module processes only annotated fields.
     * Input: defaultSecret=default-secret, specificSecret=specific-secret and normalValue=plain-value.
     * Expected output: both annotated fields use encrypted value objects and normalValue remains plain.
     */
    @Test
    void shouldEncryptOnlyAnnotatedFieldsAndHonorKeyOverride() {
        TestProperties source = new TestProperties();
        source.setDefaultSecret("default-secret");
        source.setSpecificSecret("specific-secret");
        source.setNormalValue("plain-value");

        String json = mapper.writeValueAsString(source);

        assertTrue(json.contains("\"encrypted\":true"));
        assertFalse(json.contains("default-secret"));
        assertFalse(json.contains("specific-secret"));
        assertTrue(json.contains("\"normalValue\":\"plain-value\""));

        String defaultCipherText = mapper.readTree(json).get("defaultSecret").get("value").asText();
        String specificCipherText = mapper.readTree(json).get("specificSecret").get("value").asText();
        assertEquals("default-secret", AESUtils.decrypt(defaultCipherText, "default-aes-key"));
        assertEquals("specific-secret", AESUtils.decrypt(specificCipherText, "specific-aes-key"));
    }

    /**
     * Test scenario: an encrypted JSON object is read through the same globally configured mapper.
     * Input: JSON produced from defaultSecret and specificSecret encrypted with their selected keys.
     * Expected output: deserialization restores both original plaintext values and the ordinary field.
     */
    @Test
    void shouldDecryptAnnotatedFieldsDuringDeserialization() {
        TestProperties source = new TestProperties();
        source.setDefaultSecret("default-secret");
        source.setSpecificSecret("specific-secret");
        source.setNormalValue("plain-value");

        TestProperties result = mapper.readValue(mapper.writeValueAsString(source), TestProperties.class);

        assertEquals("default-secret", result.getDefaultSecret());
        assertEquals("specific-secret", result.getSpecificSecret());
        assertEquals("plain-value", result.getNormalValue());
    }

    /**
     * Test scenario: channel configuration persistence uses Jackson convertValue instead of a JSON string round trip.
     * Input: an object with one default-key secret, one overridden-key secret and one ordinary field.
     * Expected output: Map conversion writes encrypted value objects and converting the Map back restores plaintext.
     */
    @Test
    void shouldEncryptAndDecryptDuringMapConversion() {
        TestProperties source = new TestProperties();
        source.setDefaultSecret("default-secret");
        source.setSpecificSecret("specific-secret");
        source.setNormalValue("plain-value");

        Map<String, Object> encrypted = mapper.convertValue(source, Map.class);
        TestProperties result = mapper.convertValue(encrypted, TestProperties.class);

        assertTrue(encrypted.get("defaultSecret") instanceof Map<?, ?>);
        assertEquals("default-secret", result.getDefaultSecret());
        assertEquals("specific-secret", result.getSpecificSecret());
        assertEquals("plain-value", result.getNormalValue());
    }

    @Data
    private static class TestProperties {

        @EncryptedField
        private String defaultSecret;

        @EncryptedField(aesKey = "specific-aes-key")
        private String specificSecret;

        private String normalValue;
    }
}
