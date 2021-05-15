FROM openjdk:11

COPY target/WeatherGraphApp-*.jar /usr/src/weather.jar
WORKDIR                  /usr/src/

CMD ["java", "-jar", "weather.jar" ]


HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:8085 || exit 1

