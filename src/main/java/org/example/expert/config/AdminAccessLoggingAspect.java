package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class AdminAccessLoggingAspect {

    private static final Logger logger  = LoggerFactory.getLogger(AdminAccessLoggingAspect.class);
    // Admin API 접근 시 접근 로그를 기록하는 AOP
    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public Object logAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // 요청 정보 가지고 오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI(); // 요청 URL
        String userId = request.getParameter("Authorization"); //ex)JWT 토큰에서 사용자 ID 추출
        LocalDateTime requestTime = LocalDateTime.now();// 현재시간

        logger.info("Admin API Access: UserID ={}, RequestURI = {}, RequestTime={}", userId, requestURI, requestTime);

        return joinPoint.proceed();


    }

}
