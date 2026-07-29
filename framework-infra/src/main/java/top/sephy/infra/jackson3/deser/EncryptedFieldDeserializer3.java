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
package top.sephy.infra.jackson3.deser;

import org.springframework.util.StringUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import top.sephy.infra.jackson.annotation.EncryptedField;
import top.sephy.infra.jackson3.ser.EncryptedFieldCrypto;

/**
 * Jackson 3 的加密字段反序列化器。
 */
public class EncryptedFieldDeserializer3 extends StdDeserializer<String> {

    private final String annotationAesKey;

    private final String defaultAesKey;

    private final EncryptedFieldCrypto crypto;

    public EncryptedFieldDeserializer3(String annotationAesKey, String defaultAesKey,
        EncryptedFieldCrypto crypto) {
        super(String.class);
        this.annotationAesKey = annotationAesKey;
        this.defaultAesKey = defaultAesKey;
        this.crypto = crypto;
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        JsonNode node = parser.readValueAsTree();
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isString()) {
            // 兼容旧解密器已还原的明文，以及未来的纯密文字符串格式。
            return node.asString();
        }
        if (!node.isObject() || !node.path("encrypted").asBoolean(false)) {
            return (String)context.handleUnexpectedToken(String.class, parser);
        }
        JsonNode valueNode = node.get("value");
        if (valueNode == null || !valueNode.isString() || !StringUtils.hasText(valueNode.asString())) {
            throw new IllegalStateException("加密字段的 value 不能为空");
        }
        return crypto.decrypt(valueNode.asString(), resolveAesKey());
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        if (property == null) {
            return this;
        }
        EncryptedField annotation = property.getAnnotation(EncryptedField.class);
        if (annotation == null) {
            return this;
        }
        return new EncryptedFieldDeserializer3(annotation.aesKey(), defaultAesKey, crypto);
    }

    private String resolveAesKey() {
        return StringUtils.hasText(annotationAesKey) ? annotationAesKey : defaultAesKey;
    }
}
