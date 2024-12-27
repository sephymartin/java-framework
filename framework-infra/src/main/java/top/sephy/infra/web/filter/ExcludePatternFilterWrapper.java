package top.sephy.infra.web.filter;

import java.io.IOException;
import java.util.Collection;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

/**
 * @description: 用于排除指定路径的过滤器
 */
public class ExcludePatternFilterWrapper extends OncePerRequestFilter {

    private PathMatcher pathMatcher = new AntPathMatcher();

    private Filter delegate;

    private Collection<String> excludePatterns;

    public ExcludePatternFilterWrapper(@NonNull Filter delegate, @NonNull Collection<String> excludePatterns) {
        this.delegate = delegate;
        this.excludePatterns = excludePatterns;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (excludePatterns.stream().noneMatch(pattern -> pathMatcher.match(pattern, request.getServletPath()))) {
            delegate.doFilter(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
