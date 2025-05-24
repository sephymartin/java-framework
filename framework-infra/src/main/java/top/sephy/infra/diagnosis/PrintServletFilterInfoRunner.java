package top.sephy.infra.diagnosis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;

/**
 * this class is used to print servlet filter info
 */
@Slf4j
public class PrintServletFilterInfoRunner implements CommandLineRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        String border = "*".repeat(50);
        log.info(border);

        // Get FilterRegistrationBean instances
        Map<String, FilterRegistrationBean> filterRegBeans =
            applicationContext.getBeansOfType(FilterRegistrationBean.class);

        // Get Filter instances directly
        Map<String, Filter> filterBeans = applicationContext.getBeansOfType(Filter.class);

        log.info("Found {} filter registrations and {} direct filters", filterRegBeans.size(), filterBeans.size());

        // Print FilterRegistrationBean filters
        log.info("=== Registered Filters ===");
        filterRegBeans.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getValue().getOrder()))
            .forEach(entry -> {
                FilterRegistrationBean filterBean = entry.getValue();
                log.info("Filter: [order={}] {} -> {}", filterBean.getOrder(), entry.getKey(),
                    filterBean.getUrlPatterns());
            });

        List<Filter> filtersInSecurityChain = new ArrayList<>();

        // Print direct Filter beans
        log.info("=== Direct Filter Beans ===");
        filterBeans.forEach(new BiConsumer<String, Filter>() {
            @Override
            public void accept(String name, Filter filter) {
                log.info("Filter: {} -> {}", name, filter.getClass().getName());
                if (filter instanceof FilterChainProxy chainProxy) {
                    List<SecurityFilterChain> filterChains = chainProxy.getFilterChains();
                    for (SecurityFilterChain filterChain : filterChains) {
                        log.debug("FilterChain: {}", filterChain);
                        filtersInSecurityChain.addAll(filterChain.getFilters());
                    }
                }
            }
        });

        // print each filter in filtersInSecurityChain

        log.info("=== Filters in Security Filter Chain ===");
        filtersInSecurityChain.stream().sorted(Comparator.comparing(Object::toString))
            .forEach(filter -> log.info("Filter: {}", filter.getClass().getName()));

        log.info(border);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}