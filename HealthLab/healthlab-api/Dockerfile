# ---------- Fase 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Fase 2: esecuzione ----------
FROM eclipse-temurin:21-jre-alpine

# Senza questo, la JVM userebbe UTC come fuso orario di default (le immagini
# Alpine spesso non includono i dati dei fusi orari) — ogni LocalDateTime.now()
# nel backend risulterebbe sfasato di 1-2 ore rispetto all'ora reale in Italia
# (a seconda del periodo dell'anno, ora solare o legale), con conseguenze dirette
# su qualunque controllo temporale: slot mostrati come liberi quando sono già
# passati, appuntamenti già avvenuti ancora segnalati come futuri.
RUN apk add --no-cache tzdata
ENV TZ=Europe/Rome

WORKDIR /app
COPY --from=build /app/target/healthlab-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
