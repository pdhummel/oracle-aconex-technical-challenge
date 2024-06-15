# Aconex Technical Challenge
This was written and tested with jdk 17.
Please refer to the document Technical Challenge.pdf for specfic requirements.

## How to build
From the root of the project or from your favorite IDE.

`javac -sourcepath ./src/main/java/ src/main/java/pdh/interview/aconex/Main.java -d ./target/classes`

## How to run
After compiling above.

`java -cp ./target/classes/ pdh.interview.aconex.Main`

## Testing
Tests are written for junit test framework and require 2 jar files, junit and hamcrest, to support junit testing.

### Build the tests
After compiling the main code above.

`find src/test/java/ -name "*.java" -exec javac {} -cp ./lib/junit-4.13.2.jar:./target/classes -d ./target/classes \;`

### Run the tests
After compiling the tests.

`java -cp ./target/classes/:./lib/junit-4.13.2.jar:./lib/hamcrest-core-1.3.jar  org.junit.runner.JUnitCore pdh.interview.aconex.CustomerProjectReportTest`


## Design notes
* CustomerProject is the pojo encapsulating the data structure.
  * Assumptions about entity relations are documented here.
* CustomerProjectReport produces the output for rending.
* SimpleReportWriter will accept a report and output it to stdout.
* CustomerProjectInputHandler contains most of the heavy lifting for transforming the input into useful data structures that can be consumed by our report logic.
  * This was a late game refactor and some old bits are still in CustomerProjectReport.
