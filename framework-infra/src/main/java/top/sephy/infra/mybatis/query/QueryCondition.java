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
package top.sephy.infra.mybatis.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryCondition {

    String tableAlias() default "";

    String name() default "";

    QueryOperator operator() default QueryOperator.EQ;

    /**
     *
     * @see ConverterStrategy
     * @return
     */
    ConverterStrategy converterStrategy() default ConverterStrategy.DEFAULT;

    /**
     * 值为null时是否忽略该条件
     * 
     * @return
     */
    boolean ignoreNull() default true;

    /**
     * 是否可排序, 默认不支持
     * 
     * @return
     */
    boolean sortable() default false;
}
