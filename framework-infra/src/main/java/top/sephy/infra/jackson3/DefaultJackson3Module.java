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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hashids.Hashids;

import tools.jackson.databind.module.SimpleModule;
import top.sephy.infra.jackson3.deser.EpochMillisLocalDateDeserializer3;
import top.sephy.infra.jackson3.deser.EpochMillisLocalDateTimeDeserializer3;
import top.sephy.infra.jackson3.deser.HashIdDeserializer3;
import top.sephy.infra.jackson3.ser.CustomBigDecimalSerializer3;
import top.sephy.infra.jackson3.ser.CustomLocalDateTimeSerializer;
import top.sephy.infra.jackson3.ser.EpochMillisLocalDateSerializer3;
import top.sephy.infra.jackson3.ser.HashIdSerializer3;

/**
 * Default serializer and deserializer registrations used by the framework mapper.
 */
public class DefaultJackson3Module extends SimpleModule {

    public DefaultJackson3Module() {
        super("JacksonUtilsModule");
        addSerializer(LocalDateTime.class, CustomLocalDateTimeSerializer.INSTANCE);
        addDeserializer(LocalDateTime.class, EpochMillisLocalDateTimeDeserializer3.INSTANCE);
        addSerializer(BigDecimal.class, CustomBigDecimalSerializer3.INSTANCE);
        addSerializer(LocalDate.class, EpochMillisLocalDateSerializer3.INSTANCE);
        addDeserializer(LocalDate.class, EpochMillisLocalDateDeserializer3.INSTANCE);
        addSerializer(Long.class, new HashIdSerializer3(new Hashids()));
        addDeserializer(Long.class, new HashIdDeserializer3(new Hashids()));
    }
}
