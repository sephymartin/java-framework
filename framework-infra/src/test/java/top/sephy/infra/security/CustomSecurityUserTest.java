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
package top.sephy.infra.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import top.sephy.infra.auth.AuthenticationInfo;

class CustomSecurityUserTest {

    /**
     * Test scenario: JWT claims deserialize a small numeric userId as Integer.
     * Input: attributes contain userId = Integer.valueOf(10001).
     * Expected output: getUserId returns Long.valueOf(10001) without a ClassCastException.
     */
    @Test
    void shouldReadIntegerUserIdAsLong() {
        CustomSecurityUser user = new CustomSecurityUser("admin", "", true, true, true, true,
            Collections.emptyList(), Map.of(AuthenticationInfo.KEY_USER_ID, Integer.valueOf(10001)));

        assertEquals(10001L, user.getUserId());
    }
}
