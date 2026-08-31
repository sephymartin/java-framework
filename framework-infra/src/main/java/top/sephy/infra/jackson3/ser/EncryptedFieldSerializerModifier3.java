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
package top.sephy.infra.jackson3.ser;

import java.io.Serial;
import java.util.List;

import org.springframework.util.StringUtils;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;
import top.sephy.infra.jackson.annotation.EncryptedField;

/**
 * 为标记了 {@link EncryptedField} 的 Jackson 3 字符串属性安装加密序列化器。
 */
public class EncryptedFieldSerializerModifier3 extends ValueSerializerModifier {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String defaultAesKey;

    public EncryptedFieldSerializerModifier3(String defaultAesKey) {
        this.defaultAesKey = defaultAesKey;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
        BeanDescription.Supplier beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter property : beanProperties) {
            EncryptedField annotation = property.getAnnotation(EncryptedField.class);
            if (annotation == null) {
                continue;
            }
            JavaType propertyType = property.getType();
            if (propertyType.getRawClass() != String.class) {
                throw new IllegalStateException("@EncryptedField 只支持 String 字段: " + property.getName());
            }
            ValueSerializer<Object> serializer =
                castSerializer(new EncryptedFieldSerializer3(resolveAesKey(annotation)));
            property.assignSerializer(serializer);
        }
        return beanProperties;
    }

    private String resolveAesKey(EncryptedField annotation) {
        return StringUtils.hasText(annotation.aesKey()) ? annotation.aesKey() : defaultAesKey;
    }

    @SuppressWarnings("unchecked")
    private ValueSerializer<Object> castSerializer(EncryptedFieldSerializer3 serializer) {
        return (ValueSerializer<Object>)(ValueSerializer<?>)serializer;
    }
}
