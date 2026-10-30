FROM amazoncorretto:25

COPY target/curso-java-web-0.0.1-SNAPSHOT.jar /opt/curso.jar

ENTRYPOINT exec java -jar /opt/curso.jar