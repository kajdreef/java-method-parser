# Java method parser

The project can be build using:
`./gradlew build`

Running the project can be done by running for example:
- `./gradlew method-parser:run --args='--sut <sut-path> --output <output-path>'`

Where the arguments represent the following:
- sut: A path to the system under study.
- output: A path to the directory we want to output the changes.

Where *<sut-path>* is the path to the system under test (or study) and *<output-path>* is the directory where the output json file will be stored.