FROM openjdk:11

LABEL app.name="weather"

COPY target/WeatherGraphApp-*.jar /usr/src/weather.jar
WORKDIR                  /usr/src/

CMD ["java", "-jar", "weather.jar" ]


