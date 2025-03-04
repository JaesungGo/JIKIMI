FROM tomcat:9.0-jdk17-temurin

WORKDIR /app

COPY /*.war app.war

EXPOSE 8080

CMD ["catalina.sh", "run"]