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

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;

/**
 * @author sephy
 * @date 2022-02-10 21:19
 */
public class PlaywrightObjectFactory extends BasePooledObjectFactory<Playwright> {

    private Playwright.CreateOptions createOptions;

    public PlaywrightObjectFactory(Playwright.CreateOptions createOptions) {
        this.createOptions = createOptions;
    }

    @Override
    public Playwright create() throws Exception {
        return Playwright.create(createOptions);
    }

    @Override
    public PooledObject<Playwright> wrap(Playwright playwright) {
        return new DefaultPooledPlaywright(playwright);
    }

    @Override
    public void destroyObject(PooledObject<Playwright> p) throws Exception {
        Playwright playwright = p.getObject();
        playwright.close();
    }

    @Override
    public boolean validateObject(final PooledObject<Playwright> p) {
        // p.getObject().request().newContext().get(LOCAL_VALIDATE_HTML.toURI().toString());
        APIResponse apiResponse = p.getObject().request().newContext().get("https://www.baidu.com");
        return apiResponse.ok();
    }

    private static class DefaultPooledPlaywright extends DefaultPooledObject<Playwright> {

        public DefaultPooledPlaywright(Playwright playwright) {
            super(playwright);
        }
    }

}
