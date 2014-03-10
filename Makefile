.PHONY: all clean compile javadoc package run test wc

all: compile

clean:
	mvn clean

compile:
	mvn compile

javadoc:
	mvn javadoc:aggregate

package:
	mvn package

run: compile
	javac -cp target/classes test/*.java
	java -cp `find ${HOME}/.m2/repository -name '*.jar' | tr '\n' ':'`:target/classes:test App

test:
	mvn test

wc:
	for d in src/main src/test; do \
		echo $${d}; \
		find $${d} -name '*.java' | sort | xargs wc -l; \
	done
