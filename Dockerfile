FROM java:alpine
EXPOSE 8080
COPY ca.crt /
RUN keytool -importcert -alias startssl -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -file /ca.crt -noprompt
COPY build/libs/yait-1.0-SNAPSHOT.jar /
ENTRYPOINT java -Djavax.net.debug=ssl -jar /yait-1.0-SNAPSHOT.jar
