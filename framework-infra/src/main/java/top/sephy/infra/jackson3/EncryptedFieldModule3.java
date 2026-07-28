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

import tools.jackson.databind.module.SimpleModule;
import top.sephy.infra.jackson3.deser.EncryptedFieldDeserializerModifier3;
import top.sephy.infra.jackson3.ser.EncryptedFieldSerializerModifier3;

/**
 * 全局 Jackson 3 加密字段模块。
 */
public class EncryptedFieldModule3 extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public EncryptedFieldModule3(String defaultAesKey) {
        super("EncryptedFieldModule3");
        setSerializerModifier(new EncryptedFieldSerializerModifier3(defaultAesKey));
        setDeserializerModifier(new EncryptedFieldDeserializerModifier3(defaultAesKey));
    }
}
