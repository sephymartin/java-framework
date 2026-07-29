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

import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Creates the framework's default Jackson 3 object mappers.
 */
public abstract class Jackson3ObjectMapperFactory {

    private static final String PLAYWRIGHT_CLASS_NAME = "com.microsoft.playwright.Playwright";

    public static ObjectMapper newDefaultObjectMapper() {
        return createObjectMapper(false, false, null);
    }

    /**
     * Creates the default mapper with {@link EncryptedFieldModule3} enabled.
     *
     * @param defaultAesKey default key used by fields without an annotation-level key
     * @return an object mapper with field encryption enabled
     */
    public static ObjectMapper newDefaultObjectMapper(String defaultAesKey) {
        return createObjectMapper(true, false, defaultAesKey);
    }

    /**
     * Creates the default mapper without the encrypted-field module.
     *
     * <p>This is intentionally a separate mapper construction path. Jackson modules
     * affect serializer and deserializer resolution and must not be toggled on a
     * shared mapper at request time.</p>
     *
     * @return an object mapper with field encryption disabled
     */
    public static ObjectMapper newDefaultObjectMapperWithoutEncryption() {
        return newDefaultObjectMapper();
    }

    private static ObjectMapper createObjectMapper(boolean encryptedFieldEnabled,
        boolean includeNull, String defaultAesKey) {
        JsonMapper.Builder builder = JsonMapper.builder()
            .addModule(new DefaultJackson3Module())
            .changeDefaultPropertyInclusion(oldValue -> oldValue.withValueInclusion(
                includeNull ? JsonInclude.Include.ALWAYS : JsonInclude.Include.NON_NULL))
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        if (encryptedFieldEnabled) {
            builder.addModule(new EncryptedFieldModule3(defaultAesKey));
        }
        if (ClassUtils.isPresent(PLAYWRIGHT_CLASS_NAME, Jackson3ObjectMapperFactory.class.getClassLoader())) {
            builder.addModule(new PlaywrightModule3());
        }
        return builder.build();
    }

    public static ObjectMapper newDefaultObjectMapperIncludeNull() {
        return createObjectMapper(false, true, null);
    }

    /**
     * Creates an include-null mapper with field encryption enabled.
     *
     * @param defaultAesKey default key used by fields without an annotation-level key
     * @return an include-null object mapper with field encryption enabled
     */
    public static ObjectMapper newDefaultObjectMapperIncludeNull(String defaultAesKey) {
        return createObjectMapper(true, true, defaultAesKey);
    }

    /**
     * Creates an include-null mapper without the encrypted-field module.
     *
     * @return an include-null object mapper with field encryption disabled
     */
    public static ObjectMapper newDefaultObjectMapperIncludeNullWithoutEncryption() {
        return newDefaultObjectMapperIncludeNull();
    }
}
