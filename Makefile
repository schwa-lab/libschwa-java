.PHONY: all clean compile install javadoc package test wc

all: compile

clean:
	mvn clean

compile:
	mvn compile

install:
	mvn install

javadoc:
	mvn javadoc:aggregate

package:
	mvn package

test:
	mvn test

wc:
	for d in src/main src/test; do \
		echo $${d}; \
		find $${d} -name '*.java' | sort | xargs wc -l; \
	done
