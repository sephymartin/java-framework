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
package top.sephy.infra.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    String key();

    KeyType keyType() default KeyType.DEFAULT;

    LockMode lockType() default LockMode.LOCK;

    long lockWaitTime() default 5L;

    long leaseTime() default 5L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    FailBehavior failBehavior() default FailBehavior.FAIL_FAST;
}
