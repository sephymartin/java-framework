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
package top.sephy.infra.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import top.sephy.infra.exception.JsonException;
import top.sephy.infra.jackson3.Jackson3ObjectMapperFactory;

public abstract class JacksonUtils {

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = Jackson3ObjectMapperFactory.newDefaultObjectMapper();

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER_INCLUDE_NULL =
        Jackson3ObjectMapperFactory.newDefaultObjectMapperIncludeNull();

    private static final TypeReference<HashMap<String, String>> STRING_MAP =
            new TypeReference<>() {
            };

    public static ObjectMapper newDefaultObjectMapper() {
        return Jackson3ObjectMapperFactory.newDefaultObjectMapper();
    }

    public static ObjectMapper newDefaultObjectMapper(String defaultAesKey) {
        return Jackson3ObjectMapperFactory.newDefaultObjectMapper(defaultAesKey);
    }

    public static ObjectMapper newDefaultObjectMapperWithoutEncryption() {
        return Jackson3ObjectMapperFactory.newDefaultObjectMapperWithoutEncryption();
    }

    public static String toJson(Object object) {
        return toJson(DEFAULT_OBJECT_MAPPER, object);
    }

    public static String toJson(ObjectMapper objectMapper, Object object) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new JsonException(e);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        return jsonToObject(DEFAULT_OBJECT_MAPPER, json, clazz);
    }

    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        return jsonToObject(DEFAULT_OBJECT_MAPPER, json, typeReference);
    }

    public static <T> T jsonToObject(ObjectMapper objectMapper, String json, Class<T> clazz) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    public static <T> T jsonToObject(ObjectMapper objectMapper, String json, TypeReference<T> typeReference) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    public static <T> T jsonToObject(InputStream inputStream, Class<T> clazz) {
        return jsonToObject(DEFAULT_OBJECT_MAPPER, inputStream, clazz);
    }

    public static <T> T jsonToObject(ObjectMapper objectMapper, InputStream inputStream, Class<T> clazz) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        return jsonToList(DEFAULT_OBJECT_MAPPER, json, clazz);
    }

    public static <T> List<T> jsonToList(ObjectMapper objectMapper, String json, Class<T> clazz) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            // return objectMapper.readValue(json, new TypeReference<List<T>>() {
            // });
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    public static JsonNode jsonToTree(String json) {
        return jsonToTree(DEFAULT_OBJECT_MAPPER, json);
    }

    public static JsonNode jsonToTree(ObjectMapper objectMapper, String json) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    public static <T> T treeToValue(JsonNode jsonNode, Class<T> clazz) {
        return treeToValue(DEFAULT_OBJECT_MAPPER, jsonNode, clazz);
    }

    public static <T> T treeToValue(ObjectMapper objectMapper, JsonNode jsonNode, Class<T> clazz) {
        assertObjectMapper(objectMapper);
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    public static Map<String, Object> convertToMap(Object object) {
        return convertToMap(DEFAULT_OBJECT_MAPPER, object);
    }

    public static Map<String, Object> convertToMapIncludeNull(Object object) {
        return convertToMap(DEFAULT_OBJECT_MAPPER_INCLUDE_NULL, object);
    }

    public static Map<String, Object> convertToMap(ObjectMapper objectMapper, Object object) {
        assertObjectMapper(objectMapper);
        return objectMapper.convertValue(object, Map.class);
    }

    public static Map<String, String> convertToStringMap(Object object) {
        return convertToStringMap(DEFAULT_OBJECT_MAPPER, object);
    }

    public static Map<String, String> convertToStringMapIncludeNull(Object object) {
        return convertToStringMap(DEFAULT_OBJECT_MAPPER_INCLUDE_NULL, object);
    }

    public static Map<String, String> convertToStringMap(ObjectMapper objectMapper, Object object) {
        assertObjectMapper(objectMapper);
        return objectMapper.convertValue(object, STRING_MAP);
    }

    public static <E> E convert(Object from, Class<E> to) {
        return convert(DEFAULT_OBJECT_MAPPER, from, to);
    }

    public static <E> E convert(ObjectMapper objectMapper, Object from, Class<E> to) {
        assertObjectMapper(objectMapper);
        return objectMapper.convertValue(from, to);
    }

    public static <E> E stringMapToObject(Map<String, String> map, Class<E> clazz) {
        return stringMapToObject(DEFAULT_OBJECT_MAPPER, map, clazz);
    }

    public static <E> E stringMapToObject(ObjectMapper objectMapper, Map<String, String> map, Class<E> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

    private static void assertObjectMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "ObjectMapper must not be null.");
    }

}
