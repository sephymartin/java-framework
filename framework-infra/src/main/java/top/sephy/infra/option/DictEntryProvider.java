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
package top.sephy.infra.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;

public interface DictEntryProvider<K, V> {

    /**
     * Get all options, grouped by type, must not be null
     *
     * @return
     */
    Map<String, List<DictEntry<K, V>>> optionsMap();

    /**
     * Get all item types
     *
     * @return
     */
    default Set<String> types() {
        return this.optionsMap().keySet();
    }

    /**
     * Get all options
     *
     * @return
     */
    default List<DictEntry<K, V>> allOptions() {
        List<DictEntry<K, V>> list = new ArrayList<>();
        for (Map.Entry<String, List<DictEntry<K, V>>> entry : optionsMap().entrySet()) {
            list.addAll(entry.getValue());
        }
        return list;
    }

    /**
     * Get options by type
     *
     * @param type
     * @return
     */
    default List<DictEntry<K, V>> options(String type) {
        return optionsMap().getOrDefault(type, Collections.emptyList());
    }

    default DictEntry<K, V> option(@NonNull String type, @NonNull Object k, boolean useString, boolean caseSensitive) {
        List<DictEntry<K, V>> options = options(type);
        for (DictEntry<K, V> option : options) {
            K key = option.getKey();
            if (useString) {
                String k1 = String.valueOf(key);
                String k2 = String.valueOf(k);
                if (caseSensitive && StringUtils.equals(k1, k2)) {
                    return option;
                } else if (!caseSensitive && StringUtils.equalsIgnoreCase(k1, k2)) {
                    return option;
                }
            } else if (Objects.equals(key, k)) {
                return option;
            }
        }
        return null;
    }

    default V value(@NonNull String type, @NonNull Object k, boolean useString, boolean caseSensitive, V defaultValue) {
        DictEntry<K, V> option = option(type, k, useString, caseSensitive);
        if (option != null && option.getLabel() != null) {
            return option.getLabel();
        }
        return defaultValue;
    }
}
