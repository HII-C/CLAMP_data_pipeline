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

SQL configurations:

Connect to capstone database:
USE capstone

Create sentence_text table:
Create TABLE sentence_text (
    record_id mediumint UNSIGNED NOT NULL,
    sentence_id smallint UNSIGNED NOT NULL,
    section tinyint UNSIGNED,E
    sentence text,
    PRIMARY KEY( record_id, sentence_id)
);

Create table pos:
CREATE table pos (
    pos_id tinyint UNSIGNED AUTO_INCREMENT,
    pos_text varchar(15),
    PRIMARY KEY ( pos_id )
);

Create token table:
CREATE table token (
    token_id mediumint UNSIGNED AUTO_INCREMENT,
    record_id mediumint UNSIGNED,
    sentence_id smallint UNSIGNED,
    c_start smallint UNSIGNED,
    c_end smallint UNSIGNED,
    pos_id tinyint UNSIGNED,
    FOREIGN KEY ( record_id, sentence_id ) REFERENCES sentence_text( record_id, sentence_id),
    FOREIGN KEY ( pos_id ) REFERENCES pos ( pos_id ),
    PRIMARY KEY ( token_id )
);
