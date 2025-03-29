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
package top.sephy.infra.playwright;

import java.util.concurrent.Semaphore;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sephy
 * @date 2022-02-11 22:06
 */
@Slf4j
public class PlaywrightWorkerEngine implements InitializingBean, DisposableBean {

    private GenericObjectPool<Playwright> playwrightObjectPool;

    private final PlaywrightProperties playwrightProperties;

    private Semaphore semaphore;

    private static final String MODE_POOL = "pool";

    private static final String MODE_STANDARD = "standard";

    public PlaywrightWorkerEngine(PlaywrightProperties playwrightProperties) {
        this.playwrightProperties = playwrightProperties;
    }

    public <E> E doWithPlaywright(PlaywrightPageWorker<E> worker) {
        return doWithPlaywright(new DefaultPlaywrightWorker<>(worker), playwrightProperties);
    }

    public <E> E doWithPlaywright(DefaultPlaywrightWorker<E> worker) {
        return doWithPlaywright(worker, playwrightProperties);
    }

    @SneakyThrows
    public <E> E doWithPlaywright(DefaultPlaywrightWorker<E> worker, PlaywrightProperties playwrightProperties) {

        if (MODE_POOL.equalsIgnoreCase(playwrightProperties.getMode())) {
            Playwright playwright = null;
            try {
                playwright = playwrightObjectPool.borrowObject();
                return worker.doWithPlaywright(playwright, playwrightProperties);
            } catch (Exception e) {
                throw new PlaywrightException("Execute failed.", e);
            } finally {
                if (playwright != null) {
                    playwrightObjectPool.returnObject(playwright);
                }
            }
        } else {
            semaphore.acquire();
            try (Playwright playwright = Playwright.create(playwrightProperties.getCreateOptions());) {
                return worker.doWithPlaywright(playwright, playwrightProperties);
            } finally {
                semaphore.release();
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (MODE_POOL.equalsIgnoreCase(playwrightProperties.getMode())) {
            GenericObjectPoolConfig<Playwright> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setJmxEnabled(false);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setMaxTotal(playwrightProperties.getMaxConcurrentInstance());
            playwrightObjectPool = new GenericObjectPool<>(
                new PlaywrightObjectFactory(playwrightProperties.getCreateOptions()), poolConfig);
        } else {
            semaphore = new Semaphore(playwrightProperties.getMaxConcurrentInstance());
        }
    }

    @Override
    public void destroy() throws Exception {
        if (MODE_POOL.equalsIgnoreCase(playwrightProperties.getMode())) {
            playwrightObjectPool.close();
        }
    }
}
