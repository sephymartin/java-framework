package top.sephy.infra.playwright;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sephy
 * @date 2022-02-11 22:09
 */
@Slf4j
public class DefaultPlaywrightWorker<T> {

    private PlaywrightPageWorker<T> playwrightPageWorker;

    public DefaultPlaywrightWorker(@NonNull PlaywrightPageWorker<T> playwrightPageWorker) {
        this.playwrightPageWorker = playwrightPageWorker;
    }

    public T doWithPlaywright(Playwright playwright) {
        return doWithPlaywright(playwright, null);
    }

    public T doWithPlaywright(Playwright playwright, PlaywrightProperties properties) {
        BrowserType.LaunchOptions options = properties != null ? properties.getLaunchOptions() : null;
        Browser.NewContextOptions contextOptions = properties != null ? properties.getContextOptions() : null;

        BrowserType browserType = playwright.chromium();
        if (properties != null && properties.getBrowderType() != null) {
            if ("chromium".equalsIgnoreCase(properties.getBrowderType())) {
                browserType = playwright.chromium();
            } else if ("firefox".equalsIgnoreCase(properties.getBrowderType())) {
                browserType = playwright.firefox();
            } else if ("webkit".equalsIgnoreCase(properties.getBrowderType())) {
                browserType = playwright.webkit();
            }
        }
        Browser browser = null;
        BrowserContext context = null;
        Page page = null;
        try {
            browser = browserType.launch(options); //
            context = browser.newContext(contextOptions); //
            if (properties != null && !CollectionUtils.isEmpty(properties.getInitScripts())) {
                for (String path : properties.getInitScripts()) {
                    if (ResourcePatternUtils.isUrl(path)) {
                        try (InputStream inputStream = ResourceUtils.toURI(path).toURL().openStream()) {
                            context.addInitScript(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
                        }
                    } else {
                        context.addInitScript(Paths.get(path));
                    }
                    context.addInitScript(path);
                }
            }
            page = context.newPage();
            PlaywrightProperties.PageSettings pageSettings = properties.getPageSettings();
            if (pageSettings != null) {
                page.setDefaultTimeout(pageSettings.getDefaultTimeout());
            }
            return playwrightPageWorker.doWithPage(page, context);
        } catch (Exception e) {
            if (page != null) {
                String dir = properties != null && properties.getScreenshotDir() != null
                    ? properties.getScreenshotDir().toString() : "screenshot";
                LocalDateTime now = LocalDateTime.now();
                dir = dir + "/" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                try {
                    Files.createDirectories(Paths.get(dir));
                    page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get(dir, now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".jpg"))
                        .setType(ScreenshotType.JPEG));
                } catch (IOException ex) {
                    log.error("", ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    log.error("close page error", e);
                }
            }
            if (context != null) {
                try {
                    context.close();
                } catch (Exception e) {
                    log.error("close context error", e);
                }
            }
            if (browser != null) {
                try {
                    browser.close();
                } catch (Exception e) {
                    log.error("browser close error", e);
                }
            }
        }
    }
}
