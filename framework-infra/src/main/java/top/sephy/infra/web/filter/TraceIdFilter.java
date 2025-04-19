package top.sephy.infra.web.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.agent.core.context.ids.GlobalIdGenerator;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import top.sephy.infra.consts.GlobalSystemConstants;

/**
 * 确保此过滤器在最前面
 */
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String tid = null;
        try {
            tid = request.getHeader(GlobalSystemConstants.TRACE_ID_NAME);
            if (StringUtils.isBlank(tid)) {
                tid = GlobalIdGenerator.generate();
            }
            MDC.put(GlobalSystemConstants.TRACE_ID_NAME, tid);
        } finally {
            // 在响应头中添加 traceId
            if (StringUtils.isNoneBlank(tid)) {
                response.setHeader(GlobalSystemConstants.TRACE_ID_NAME, tid);
            }
            filterChain.doFilter(request, response);
            MDC.remove(GlobalSystemConstants.TRACE_ID_NAME);
        }
    }
}
