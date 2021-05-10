
all:
	cd java && \
	mvn package
	cp java/target/java-1.0-SNAPSHOT.one-jar.jar src/gavel_owl/jars/api.jar
