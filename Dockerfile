FROM java:alpine
EXPOSE 8080
COPY ca.crt /
RUN keytool -importcert -alias startssl -storepass lolxdpasswd -file /ca.crt -noprompt
COPY build/libs/yait-1.0-SNAPSHOT.jar /
ENTRYPOINT java -jar /yait-1.0-SNAPSHOT.jar

