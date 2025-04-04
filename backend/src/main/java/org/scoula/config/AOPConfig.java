package org.scoula.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig {


    @Bean
    public AOPConfig.ChatBotAspectClass aspect(){
        return new AOPConfig.ChatBotAspectClass();
    }

    @Aspect
    @Slf4j
    public static class ChatBotAspectClass{
        @Pointcut("execution(* org.scoula.chat..*.*(..))")
        private void chatbotPointcut() {}

        @Around("chatbotPointcut()")
        public Object loggingAround(ProceedingJoinPoint joinPoint) throws Throwable {
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();

            Object[] args = joinPoint.getArgs();
            String params = Stream.of(args)
                    .map(this::convertToString)
                    .collect(Collectors.joining(", "));


            log.info("[{}] {}: -> parameters: {}",
                    className, methodName, params);

            long startTime = System.currentTimeMillis();

            Object result = null;
            try {
                result = joinPoint.proceed();

                // Mono 타입 처리 (챗봇 비동기 처리 방식)
                if (result instanceof Mono) {
                    return ((Mono<?>) result).doOnSuccess(value -> {
                        long endTime = System.currentTimeMillis();
                        long executionTime = endTime - startTime;
                        log.info("[{}] {}: result: {}, executionTime: {}",
                                className, methodName, convertToString(value), executionTime);
                    }).doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        long executionTime = endTime - startTime;
                        log.error("[{}] {}: Exception: {}, executionTime: {}",
                                className, methodName, error.getMessage(), executionTime);
                    });
                }

                // 일반 타입 처리 (Mono가 아닌 타입)
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                log.info("[{}] {}: result: {}, executionTime: {}",
                        className, methodName, convertToString(result), executionTime);
                return result;

            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                log.error("[{}] {}: Exception: {}, executionTime: {}",
                        className, methodName, e.getMessage(), executionTime);
                throw e;
            }
        }

        private String convertToString(Object object){
            // null 일 때
            if(object == null){
                return null;
            }

            // 배열일 떄
            if(object.getClass().isArray()){
                return Arrays.toString((Object[]) object);
            }

            return object.toString();
        }
    }

}