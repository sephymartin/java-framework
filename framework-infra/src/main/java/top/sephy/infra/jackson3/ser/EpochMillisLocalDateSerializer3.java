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

import java.time.LocalDate;
import java.time.ZoneId;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializes LocalDate as an epoch-millisecond timestamp in the system time zone.
 */
public class EpochMillisLocalDateSerializer3 extends StdSerializer<LocalDate> {

    public static final EpochMillisLocalDateSerializer3 INSTANCE = new EpochMillisLocalDateSerializer3();

    private EpochMillisLocalDateSerializer3() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializationContext provider) {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeNumber(value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}
