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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import top.sephy.infra.exception.SystemException;

/**
 * 使用 JDK 标准库执行与 MySQL 两参数 {@code AES_ENCRYPT(value, key)} 兼容的加解密。
 *
 * 协议固定使用 UTF-8、AES-128-ECB、PKCS padding 和标准 Base64。密钥按 MySQL
 * 两参数模式的规则折叠到 16 字节 AES 密钥。
 */
@Component
public class EncryptedFieldCrypto {

    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private static final String KEY_ALGORITHM = "AES";

    private static final int AES_BLOCK_SIZE_BYTES = 16;

    public String encrypt(String plainText, String aesKey) {
        validateInput(plainText, aesKey);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key(aesKey));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (GeneralSecurityException exception) {
            throw new SystemException("字段加密失败", exception);
        }
    }

    public String decrypt(String encodedCipherText, String aesKey) {
        validateInput(encodedCipherText, aesKey);
        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(encodedCipherText);
        } catch (IllegalArgumentException exception) {
            throw new SystemException("字段密文不是有效的 Base64", exception);
        }
        if (payload.length == 0 || payload.length % AES_BLOCK_SIZE_BYTES != 0) {
            throw new SystemException("字段密文长度无效");
        }
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(aesKey));
            return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new SystemException("字段解密失败", exception);
        }
    }

    private SecretKeySpec key(String aesKey) {
        byte[] derivedKey = new byte[AES_BLOCK_SIZE_BYTES];
        byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
        for (int index = 0; index < keyBytes.length; index++) {
            derivedKey[index % AES_BLOCK_SIZE_BYTES] ^= keyBytes[index];
        }
        return new SecretKeySpec(derivedKey, KEY_ALGORITHM);
    }

    private void validateInput(String value, String aesKey) {
        if (value == null) {
            throw new SystemException("字段加解密值不能为空");
        }
        if (!StringUtils.hasText(aesKey)) {
            throw new SystemException("未配置字段加密 AES 密钥");
        }
    }
}
