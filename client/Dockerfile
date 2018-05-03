# Intermediate build container
FROM openjdk:8-jdk as builder

LABEL builder=true
COPY src /build/src
WORKDIR /build
RUN set -x && \
	mkdir /build/classes && \
	cd src/com/kwkoo && \
	javac -cp /build/src:/build/classes -d /build/classes Temp.java && \
	cd /build/classes && \
	jar -cf /build/tempsensor.jar .

# Actual container to run the compiled JAR
FROM openjdk:8-jre
WORKDIR /
COPY --from=builder /build/tempsensor.jar /
COPY runsensor /
RUN set -x && \
	chmod 755 /runsensor
ENTRYPOINT ["/runsensor"]

