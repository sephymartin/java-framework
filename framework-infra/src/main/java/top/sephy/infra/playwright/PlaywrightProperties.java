package top.sephy.infra.playwright;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import lombok.Data;

/**
 * @author sephy
 * @date 2021-07-25 10:46
 */
@Data
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties implements InitializingBean {

    private int maxConcurrentInstance = 5;

    private String mode = "standard";

    private List<String> initScripts;

    private Path screenshotDir;

    @NestedConfigurationProperty
    private ProxySettings proxySettings;

    @NestedConfigurationProperty
    private ScreenSizeSettings screenSizeSettings;

    @NestedConfigurationProperty
    private ViewportSizeSettings viewportSizeSettings;

    @NestedConfigurationProperty
    private PageSettings pageSettings = new PageSettings();

    private Playwright.CreateOptions createOptions = new Playwright.CreateOptions();

    private BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();

    private Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

    private String browderType;

    private List<String> routeSettings;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (screenSizeSettings != null) {
            contextOptions.setScreenSize(screenSizeSettings.width, screenSizeSettings.height);
        }

        if (viewportSizeSettings != null) {
            contextOptions.setViewportSize(viewportSizeSettings.width, viewportSizeSettings.height);
        }

        if (screenshotDir != null) {
            Files.createDirectories(screenshotDir);
        }

    }

    @Data
    public static class ProxySettings {

        private boolean enableProxy = false;

        private String server;

        private String username;

        private String password;
    }

    @Data
    public static class ScreenSizeSettings {

        /**
         * page width in pixels.
         */
        public int width = 1920;
        /**
         * page height in pixels.
         */
        public int height = 1080;
    }

    @Data
    public static class ViewportSizeSettings {

        public int width = 1920;
        /**
         * page height in pixels.
         */
        public int height = 1080;
    }

    @Data
    public static class PageSettings {

        double defaultTimeout = 3000D;
    }
}
