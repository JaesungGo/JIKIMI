FROM tomcat:9.0-jdk17-temurin

# 애플리케이션에 필요한 디렉토리 생성
RUN mkdir -p /usr/local/tomcat/logs

# Log4j 설정 파일 생성 (WAR 내에 없는 경우)
RUN echo "log4j.rootLogger=INFO, stdout\n\
log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n\
log4j.appender.stdout.Target=System.out\n\
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout\n\
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n\n\
log4j.logger.org.springframework=DEBUG" > /usr/local/tomcat/lib/log4j.properties

RUN ./gradlew clean build -x test

# WAR 파일 복사 (실제 WAR 파일명으로 수정)
COPY /build/libs/backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# 환경 변수 설정
ENV JAVA_OPTS="-Dlog4j.debug=true -Dlog4j.configuration=file:/usr/local/tomcat/lib/log4j.properties -Dspring.profiles.active=default"

EXPOSE 8080
CMD ["catalina.sh", "run"]

