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

import org.springframework.stereotype.Component;

import top.sephy.infra.utils.AESUtils;

/**
 * @deprecated 使用 {@link AESUtils}。此类仅用于兼容现有业务侧的构造器注入。
 */
@Deprecated(since = "4.0", forRemoval = true)
@Component
public class EncryptedFieldCrypto {

    /**
     * @deprecated 使用 {@link AESUtils#encrypt(String, String)}。
     */
    @Deprecated(since = "4.0", forRemoval = true)
    public String encrypt(String plainText, String aesKey) {
        return AESUtils.encrypt(plainText, aesKey);
    }

    /**
     * @deprecated 使用 {@link AESUtils#decrypt(String, String)}。
     */
    @Deprecated(since = "4.0", forRemoval = true)
    public String decrypt(String encodedCipherText, String aesKey) {
        return AESUtils.decrypt(encodedCipherText, aesKey);
    }
}
