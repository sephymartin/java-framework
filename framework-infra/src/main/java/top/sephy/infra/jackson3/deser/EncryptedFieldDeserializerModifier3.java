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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.deser.BeanDeserializerBuilder;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import top.sephy.infra.jackson.annotation.EncryptedField;
import top.sephy.infra.jackson3.ser.EncryptedFieldCrypto;

/**
 * 为标记了 {@link EncryptedField} 的 Jackson 3 字符串属性安装解密反序列化器。
 */
public class EncryptedFieldDeserializerModifier3 extends ValueDeserializerModifier {

    private static final long serialVersionUID = 1L;

    private final String defaultAesKey;

    private final EncryptedFieldCrypto crypto;

    public EncryptedFieldDeserializerModifier3(String defaultAesKey) {
        this.defaultAesKey = defaultAesKey;
        this.crypto = new EncryptedFieldCrypto();
    }

    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription.Supplier beanDesc,
        BeanDeserializerBuilder builder) {
        List<SettableBeanProperty> encryptedProperties = new ArrayList<>();
        Iterator<SettableBeanProperty> properties = builder.getProperties();
        while (properties.hasNext()) {
            SettableBeanProperty property = properties.next();
            EncryptedField annotation = property.getAnnotation(EncryptedField.class);
            if (annotation == null) {
                continue;
            }
            JavaType propertyType = property.getType();
            if (propertyType.getRawClass() != String.class) {
                throw new IllegalStateException("@EncryptedField 只支持 String 字段: " + property.getName());
            }
            encryptedProperties.add(property.withValueDeserializer(new EncryptedFieldDeserializer3(
                annotation.aesKey(), defaultAesKey, crypto)));
        }
        for (SettableBeanProperty property : encryptedProperties) {
            builder.addOrReplaceProperty(property, true);
        }
        return builder;
    }
}
