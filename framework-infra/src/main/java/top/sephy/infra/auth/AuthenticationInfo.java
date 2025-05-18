/*
 * Copyright 2022-2025 sephy.top
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.sephy.infra.auth;

public interface AuthenticationInfo {

    String KEY_USER_ID = "userId";

    String KEY_NICKNAME = "nickname";

    String KEY_USERNAME = "username";

    Long getUserId();

    String getUsername();

    /**
     * 获取当前用户名称
     * 
     * @return
     */
    String getNickname();
}
