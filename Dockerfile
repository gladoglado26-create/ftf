# Multi-stage production image for the Ktor backend
FROM gradle:8.9-jdk17 AS build
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY src ./src
RUN gradle clean installDist --no-daemon --console=plain

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --create-home --shell /usr/sbin/nologin appuser
COPY --from=build /app/build/install/social-platform-backend ./
ENV PORT=8080
EXPOSE 8080
USER appuser
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 CMD ["/bin/sh", "-c", "wget -qO- http://127.0.0.1:${PORT}/health || exit 1"]
CMD ["/app/bin/social-platform-backend"]
