package top.sephy.infra.playwright;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sephy
 * @date 2022-02-27 10:07
 */
public class PlaywrightModule extends SimpleModule {

    public PlaywrightModule() {
        super("PlaywrightModule", new Version(0, 1, 0, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixInAnnotations(com.microsoft.playwright.options.ViewportSize.class, ViewportSizeMixIn.class);
        context.setMixInAnnotations(com.microsoft.playwright.options.ScreenSize.class, ScreenSizeMixIn.class);
        context.setMixInAnnotations(com.microsoft.playwright.options.Cookie.class, CookieMixIn.class);
        context.setMixInAnnotations(com.microsoft.playwright.options.Proxy.class, ProxyMixIn.class);
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

    @Slf4j
    public static class ProxyMixIn {
        @JsonCreator
        public ProxyMixIn(@JsonProperty("server") String server) {
            log.info("ProxyMixIn called!");
        }
    }
}
