# Studying the effect of tests on the stability of a method.

The project can be build using:
`./gradlew build`

The experiment can be run by running the following script:
`./run-experiment2 <project_path> <output_dir> <epoch> <allchanges>`

Where the arguments represent the following:
- project_path: A path to the system under study.
- output_dir: A path to the directory we want to output the changes.
- epoch: An integer representing the numbeer of days between the two commits.
- allchanges: An integer (0 or 1) to represent if we want to limit the changes to the methods made between the two commits or all changes since introduction of the method.

Where *<sut-path>* is the path to the system under study.