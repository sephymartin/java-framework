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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import top.sephy.infra.exception.SystemException;

/**
 * 使用 JDK 标准库执行带 {@code @EncryptedField} 字段的 AES-GCM 加解密。
 */
@Component
public class EncryptedFieldCrypto {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final String KEY_ALGORITHM = "AES";

    private static final int NONCE_LENGTH_BYTES = 12;

    private static final int TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plainText, String aesKey) {
        validateInput(plainText, aesKey);
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key(aesKey), new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + cipherText.length)
                .put(nonce)
                .put(cipherText)
                .array());
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
        if (payload.length <= NONCE_LENGTH_BYTES) {
            throw new SystemException("字段密文长度无效");
        }

        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        byte[] cipherText = new byte[payload.length - NONCE_LENGTH_BYTES];
        System.arraycopy(payload, 0, nonce, 0, nonce.length);
        System.arraycopy(payload, nonce.length, cipherText, 0, cipherText.length);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(aesKey), new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new SystemException("字段解密失败", exception);
        }
    }

    private SecretKeySpec key(String aesKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(aesKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, KEY_ALGORITHM);
        } catch (GeneralSecurityException exception) {
            throw new SystemException("无法初始化字段加密密钥", exception);
        }
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
