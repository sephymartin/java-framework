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
