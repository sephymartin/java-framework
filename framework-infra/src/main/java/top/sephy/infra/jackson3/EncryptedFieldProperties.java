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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Jackson 3 加密字段的全局配置。
 */
@Data
@ConfigurationProperties(prefix = "infra.jackson.encrypted")
public class EncryptedFieldProperties {

    /**
     * 未通过注解指定时使用的默认 AES 密钥。
     */
    private String aesKey;
}
