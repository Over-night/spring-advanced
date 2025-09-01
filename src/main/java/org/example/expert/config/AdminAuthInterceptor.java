package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public AdminAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        final String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or Invalid Token");
            return false;
        }

        final String token = auth.substring(7);
        if(!jwtUtil.validateToken(token)){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Expired Token");
            return false;
        }

        final Long userId = jwtUtil.getUserId(token);
        final UserRole role = jwtUtil.getUserRole(token);

        if (!role.equals(UserRole.ADMIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin Role Required");
            return false;
        }

        final Long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        request.setAttribute("userId", userId);

        log.info("[REQ] {} {} | startAt={} userId={}",
                request.getMethod(),
                request.getRequestURL(),
                startTime,
                userId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception e) throws Exception {
        final Long userId = (Long) request.getAttribute("userId");
        final Long startTime = (Long) request.getAttribute("startTime");
        final long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

        log.info("[RES] {} {} | status={} duration={} userId={}{}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                userId,
                e != null ? " e=" + e.getClass().getSimpleName() : "");
    }
}
