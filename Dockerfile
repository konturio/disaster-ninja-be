FROM openjdk:17-alpine as builder
WORKDIR /application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-alpine
RUN addgroup -S application && adduser -S application -G application
USER application:application
WORKDIR application
EXPOSE 8627
COPY --from=builder /application/dependencies ./
COPY --from=builder /application/spring-boot-loader ./
COPY --from=builder /application/snapshot-dependencies ./
COPY --from=builder /application/application ./
ENTRYPOINT ["java", "-Dlogging.config=classpath:logback.docker.xml", "org.springframework.boot.loader.JarLauncher"]