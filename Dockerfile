FROM openjdk:11

COPY target/WeatherGraphApp-*.jar /usr/src/weather.jar
WORKDIR                  /usr/src/

CMD ["java", "-jar", "weather.jar" ]


