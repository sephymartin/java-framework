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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.JacksonModule;

/**
 * Jackson 3 加密字段自动配置。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JacksonModule.class)
@EnableConfigurationProperties(EncryptedFieldProperties.class)
public class EncryptedFieldAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EncryptedFieldModule3.class)
    @ConditionalOnProperty(prefix = "infra.jackson.encrypted", name = "enabled", havingValue = "true",
        matchIfMissing = true)
    public EncryptedFieldModule3 encryptedFieldModule3(EncryptedFieldProperties properties) {
        return new EncryptedFieldModule3(properties.getAesKey());
    }
}
