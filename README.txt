Interface for parsing parsed CLAMP data into SQL queries in the schema defined in the Data Overview.

Parsable line types:
    - NamedEntity
    - Relation
    - Sentence
    - Token

The executable jar for this application is located in PittParser/out/artifacts/~.jar
This jar can be run with:
1. cd to the above directory
2. java -jar name_of_jar.jar < database UID > < database password >

To configure build on IDE:
1. Add the .jar files in PittParser/external_dependencies to the build path
2. Set the out class path to the PittParser/out folder
3. Configure the output jar to the main class in PittParser/src/parser/Parser

