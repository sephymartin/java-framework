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
package top.sephy.infra.jackson3.deser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;

/**
 * Reads epoch-millisecond timestamps and the standard LocalDateTime representation.
 */
public class EpochMillisLocalDateTimeDeserializer3 extends LocalDateTimeDeserializer {

    public static final EpochMillisLocalDateTimeDeserializer3 INSTANCE =
        new EpochMillisLocalDateTimeDeserializer3(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private EpochMillisLocalDateTimeDeserializer3(DateTimeFormatter formatter) {
        super(formatter);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), ZoneId.systemDefault());
        }
        return super.deserialize(p, ctxt);
    }
}
