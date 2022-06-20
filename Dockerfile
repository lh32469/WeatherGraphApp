FROM openjdk:11

RUN rm /etc/localtime
RUN ln -s /usr/share/zoneinfo/PST8PDT /etc/localtime

COPY target/WeatherGraphApp-*.jar /usr/src/weather.jar
WORKDIR                  /usr/src/

ENV _JAVA_OPTIONS="-Xmx256m"

CMD ["java", "-jar", "weather.jar" ]

HEALTHCHECK --interval=15s --timeout=3s \
  CMD curl -f http://localhost:8085/actuator/health | grep UP || exit 1

#HEALTHCHECK --interval=5m --timeout=3s \
#  CMD curl -f http://localhost:8085 || exit 1

