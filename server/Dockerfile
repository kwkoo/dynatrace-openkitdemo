# Intermediate build container
FROM jetty:9-jre8 as builder

LABEL builder=true
USER root
RUN mkdir -p /buildout/WEB-INF/classes
COPY web.xml /buildout/WEB-INF/
COPY src /src

RUN set -x && \
	apt-get update && \
	apt-get install -y openjdk-8-jdk && \
	cd /src/demo && \
	javac \
	  -cp /usr/local/jetty/lib/servlet-api-3.1.jar:/src \
	  -d /buildout/WEB-INF/classes \
	  TempServer.java && \
	cd /buildout && \
	jar -cf /temp.war .

# Actual container to run the compiled JAR
FROM jetty:9-jre8
COPY --from=builder /temp.war /var/lib/jetty/webapps/

