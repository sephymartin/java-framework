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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson 3 BigDecimal serialization rules.
 */
public class CustomBigDecimalSerializer3 extends StdSerializer<BigDecimal> {

    private static final int CURRENCY_SCALE = 2;

    public static final CustomBigDecimalSerializer3 INSTANCE = new CustomBigDecimalSerializer3(null);

    private final DecimalFormat decimalFormat;

    private CustomBigDecimalSerializer3(DecimalFormat decimalFormat) {
        super(BigDecimal.class);
        this.decimalFormat = decimalFormat;
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext serializers) {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (decimalFormat != null) {
            gen.writeNumber(decimalFormat.format(value));
            return;
        }
        if (value.scale() < CURRENCY_SCALE) {
            gen.writeString(value.setScale(CURRENCY_SCALE, RoundingMode.HALF_UP).toPlainString());
            return;
        }
        gen.writeString(value.toPlainString());
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
        if (property == null) {
            return this;
        }
        JsonFormat.Value format = property.findPropertyFormat(prov.getConfig(), handledType());
        if (format == null || !format.hasPattern()) {
            return this;
        }
        DecimalFormat customFormat = new DecimalFormat(format.getPattern());
        customFormat.setRoundingMode(RoundingMode.HALF_UP);
        return new CustomBigDecimalSerializer3(customFormat);
    }
}
