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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 3 MixIns for Playwright value types.
 */
public class PlaywrightModule3 extends SimpleModule {

    public PlaywrightModule3() {
        super("PlaywrightModule", new Version(0, 0, 1, null, null, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(com.microsoft.playwright.options.ViewportSize.class, ViewportSizeMixIn.class);
        context.setMixIn(com.microsoft.playwright.options.ScreenSize.class, ScreenSizeMixIn.class);
        context.setMixIn(com.microsoft.playwright.options.Cookie.class, CookieMixIn.class);
    }

    @Slf4j
    public static class ViewportSizeMixIn {
        @JsonCreator
        public ViewportSizeMixIn(@JsonProperty("width") int width, @JsonProperty("height") int height) {
            log.info("ViewportSizeMixIn called!");
        }
    }

    @Slf4j
    public static class ScreenSizeMixIn {
        @JsonCreator
        public ScreenSizeMixIn(@JsonProperty("width") int width, @JsonProperty("height") int height) {
            log.info("ViewportSizeMixIn called!");
        }
    }

    @Slf4j
    public static class CookieMixIn {
        @JsonCreator
        public CookieMixIn(@JsonProperty("name") String name, @JsonProperty("value") String value) {
            log.info("CookieMixIn called!");
        }
    }
}
