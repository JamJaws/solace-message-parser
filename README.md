# Solace message parser

This project aims to parse Solace messages and extract the message content and metadata.

Example of input solace dump file: [input_1.txt](app%2Fsrc%2Ftest%2Fresources%2Finput%2Finput_1.txt)

## Prerequisites

- Java 21

## Build

```shell
./gradlew build
```

## Run

Put the Solace message dump files you wish to parse the [input](input) folder run the following command:

```shell
./gradlew run
```

The extracted json will be put in the [output](output) folder and metadata about the messages printed to the console.
