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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import top.sephy.infra.exception.SystemException;

class EncryptedFieldCryptoTest {

    private static final String KEY = "secret";

    private final EncryptedFieldCrypto crypto = new EncryptedFieldCrypto();

    /**
     * Test scenario: MySQL-compatible AES_ENCRYPT uses key "secret" and plaintext "hello".
     * Input: UTF-8 plaintext "hello" and key "secret".
     * Expected output: the independently calculated Base64 ciphertext "wpDott5OpXc0FOAZ/n8Xow==".
     */
    @Test
    void shouldEncryptToMySqlCompatibleFixedVector() {
        assertEquals("wpDott5OpXc0FOAZ/n8Xow==", crypto.encrypt("hello", KEY));
    }

    /**
     * Test scenario: a fixed AES_ENCRYPT result is passed from MySQL to Java.
     * Input: Base64 ciphertext "wpDott5OpXc0FOAZ/n8Xow==" and key "secret".
     * Expected output: plaintext "hello".
     */
    @Test
    void shouldDecryptMySqlCompatibleFixedVector() {
        assertEquals("hello", crypto.decrypt("wpDott5OpXc0FOAZ/n8Xow==", KEY));
    }

    /**
     * Test scenario: a MySQL-compatible fixed vector contains UTF-8 Chinese text and an emoji.
     * Input: plaintext "你好，世界🌏" and key "secret".
     * Expected output: Base64 ciphertext "+mJsS7jKdvk4NHmxJYSNRJYp+Cmqu3WccAtZEKR1DW0="
     * and the exact original Unicode plaintext when decrypted.
     */
    @Test
    void shouldUseUtf8ForChineseMySqlCompatibleVector() {
        String plainText = "你好，世界🌏";
        String cipherText = "+mJsS7jKdvk4NHmxJYSNRJYp+Cmqu3WccAtZEKR1DW0=";

        assertEquals(cipherText, crypto.encrypt(plainText, KEY));
        assertEquals(plainText, crypto.decrypt(cipherText, KEY));
    }

    /**
     * Test scenario: an empty String is a valid plaintext but still receives one PKCS padding block.
     * Input: empty plaintext and key "secret".
     * Expected output: a 16-byte ciphertext that decrypts to the empty String.
     */
    @Test
    void shouldRoundTripEmptyTextWithPaddingBlock() {
        String cipherText = crypto.encrypt("", KEY);

        assertEquals(16, Base64.getDecoder().decode(cipherText).length);
        assertEquals("", crypto.decrypt(cipherText, KEY));
    }

    /**
     * Test scenario: plaintext lengths around AES block boundaries use PKCS padding correctly.
     * Input: ASCII plaintext lengths 15, 16, 17 and 32 with key "secret".
     * Expected output: every value round-trips and ciphertext lengths are 16, 32, 32 and 48 bytes.
     */
    @Test
    void shouldRoundTripPlaintextAtBlockBoundaries() {
        int[] lengths = {15, 16, 17, 32};
        int[] expectedCiphertextLengths = {16, 32, 32, 48};

        for (int index = 0; index < lengths.length; index++) {
            String plainText = "x".repeat(lengths[index]);
            String cipherText = crypto.encrypt(plainText, KEY);

            assertEquals(expectedCiphertextLengths[index], Base64.getDecoder().decode(cipherText).length);
            assertEquals(plainText, crypto.decrypt(cipherText, KEY));
        }
    }

    /**
     * Test scenario: JSON text is treated as ordinary UTF-8 plaintext by the protocol.
     * Input: JSON containing a bank card number and a Chinese cardholder name.
     * Expected output: the exact JSON text is restored after encryption and decryption.
     */
    @Test
    void shouldRoundTripJsonText() {
        String plainText = "{\"bankCardNumber\":\"6222021234567890\",\"cardholderName\":\"张三\"}";

        assertEquals(plainText, crypto.decrypt(crypto.encrypt(plainText, KEY), KEY));
    }

    /**
     * Test scenario: malformed Base64 input reaches the decryption boundary.
     * Input: non-Base64 text "not-base64" and a valid key.
     * Expected output: the existing SystemException boundary with the Base64-specific message.
     */
    @Test
    void shouldRejectInvalidBase64() {
        SystemException exception = assertThrows(SystemException.class,
            () -> crypto.decrypt("not-base64", KEY));

        assertEquals("字段密文不是有效的 Base64", exception.getMessage());
    }

    /**
     * Test scenario: Base64 decodes to bytes that are not a complete AES block.
     * Input: Base64 encoding of the five-byte value "short" and a valid key.
     * Expected output: the existing invalid-ciphertext-length SystemException boundary.
     */
    @Test
    void shouldRejectCiphertextWithInvalidLength() {
        String cipherText = Base64.getEncoder().encodeToString("short".getBytes(StandardCharsets.UTF_8));

        SystemException exception = assertThrows(SystemException.class,
            () -> crypto.decrypt(cipherText, KEY));

        assertEquals("字段密文长度无效", exception.getMessage());
    }

    /**
     * Test scenario: a ciphertext is decrypted with a different non-empty key.
     * Input: ciphertext encrypted with "secret" and decryption key "wrong-key".
     * Expected output: decryption fails at the existing SystemException boundary.
     */
    @Test
    void shouldRejectWrongKey() {
        String cipherText = crypto.encrypt("sensitive-value", KEY);

        assertThrows(SystemException.class, () -> crypto.decrypt(cipherText, "wrong-key"));
    }

    /**
     * Test scenario: a blank key is not a usable encryption key.
     * Input: plaintext "value" and a whitespace-only key.
     * Expected output: the existing missing-key SystemException boundary.
     */
    @Test
    void shouldRejectBlankKey() {
        SystemException exception = assertThrows(SystemException.class,
            () -> crypto.encrypt("value", "   "));

        assertEquals("未配置字段加密 AES 密钥", exception.getMessage());
    }
}
