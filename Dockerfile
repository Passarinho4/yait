FROM java:8
EXPOSE 8080
COPY build/libs/yait-1.0-SNAPSHOT.jar /
ENTRYPOINT java -jar /yait-1.0-SNAPSHOT.jar

