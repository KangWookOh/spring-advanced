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
//Aspect 관심사를 모듈화 한 클래스
// 로깅 , 보안 등 같은 기능이 여러 클래스에 걸쳐 사용되는경우 모듈화 할수 있음
@Aspect
@Component
public class AdminAccessLoggingAspect {

    private static final Logger logger  = LoggerFactory.getLogger(AdminAccessLoggingAspect.class);
    // Admin API 접근 시 접근 로그를 기록하는 AOP
    // Around 대상 메서드에 실행 전과 후 모두에서 로직을 실행할수 있고
    // ProceedingJoinPoint 메서드를 통해 직접 호출 할수 있는 가장 강력한 어드바이스로써 메서드 실행 전후로 로직에 넣을 수 있다.
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
