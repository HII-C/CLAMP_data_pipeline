Interface for parsing parsed CLAMP data into SQL queries in the schema defined in the Data Overview.

Parsable line types:
    - NamedEntity
    - Relation
    - Sentence
    - Token

** Note that each non-sentence data type is mapped to a corresponding sentence depending on the start/end token.
However, there are cases where the start-end token can be mapped to MULTIPLE sentences. We have opted to match
these unique cases to the right-most sentence that falls within the range of the start/end tokens.

The application is split into two major parts:
(a) Generate UMLS encoded text using the CLAMP tool (in the form of a jar)
(b) Parse CLAMP-generated text into SQL queries that will be run on a SQL server.

// =======================================================================================

(a) To run the UMLS generation,
1. cd to the CLAMP_PIPELINE/scripts folder
2. Generate batches from the input data. This will help us multithread/multiprocess to improve speed.
    - python input_data_subdirectory_split.py
    - Optionally, you can set the number of batches to be created within the file.
3. Run the pipeline on a batch or set of batches
    - python test_batch_clamp_run.py
4. Move the completed inputs to a separate folder ( to save progress )
    - python move_completed.py

// =======================================================================================

(b) To run the output parsing/SQL generation pipeline, run the run_pitt_parser.py in the scripts folder
1. cd to the CLAMP_PIPELINE/scripts folder
2. python run_pitt_parser.py <db_username> <db_password>

* Potential errors will be logged into CLAMP_PIPELINE/parselogs/condensed.txt
- You can set the number of threads

Alernatively, if you want to run the
The executable jar for this application is located in PittParser/out/artifacts/~.jar
This jar can be run with:
1. cd to the above directory
2. java -jar name_of_jar.jar < database UID > < database password > <path_to_output_file> <path_to_input_file>

// =======================================================================================

All files in src are packaged into a jar file that encapsulates all parsing logic. The app starting point
can be found in src/parser/Parser.java

To configure build on IDE:
1. Add the .jar files in PittParser/external_dependencies to the build path
2. Set the out class path to the PittParser/out folder
3. Configure the output jar to the main class in PittParser/src/parser/Parser

// =======================================================================================

SQL configurations: [ Can also be found in Data Overview ]

Connect to capstone database:
USE capstone

Create sentence_text table:
CREATE TABLE sentence_text (
    record_id mediumint UNSIGNED NOT NULL,
    sentence_id smallint UNSIGNED NOT NULL,
    section_id tinyint UNSIGNED,
    sentence text,
    FOREIGN KEY( section_id ) REFERENCES section ( section_id ),
    PRIMARY KEY( record_id, sentence_id)
);

Create sentence_index table:
CREATE table sentence_index (
    record_id mediumint UNSIGNED NOT NULL,
    sentence_id smallint UNSIGNED NOT NULL,
    section_id tinyint UNSIGNED,
    c_start tinyint UNSIGNED,
    c_end tinyint UNSIGNED,
    FOREIGN KEY( section_id ) REFERENCES sentence_section ( section_id ),
    PRIMARY KEY( record_id, sentence_id )
);

Create sentence_section table:
CREATE table sentence_section (
    section_id tinyint UNSIGNED AUTO_INCREMENT,
    section_text text NOT NULL,
    PRIMARY KEY( section_id )
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

Create concept_semantic table:
CREATE table concept_semantic (
    semantic_id tinyint UNSIGNED AUTO_INCREMENT,
    semantic_text varchar(15),
    PRIMARY KEY ( semantic_id )
);

Create concept_assertion table:
CREATE table concept_assertion (
    assertion_id tinyint UNSIGNED AUTO_INCREMENT,
    assertion_text varchar(15),
    PRIMARY KEY ( assertion_id )
);

Create concepts table:
CREATE table concepts (
    concept_id mediumint UNSIGNED AUTO_INCREMENT,
    record_id mediumint UNSIGNED,
    sentence_id smallint UNSIGNED,
    cui char(8),
    c_start smallint UNSIGNED,
    c_end smallint UNSIGNED,
    text varchar(200),
    semantic tinyint UNSIGNED,
    assertion tinyint UNSIGNED,
    FOREIGN KEY ( record_id, sentence_id ) REFERENCES sentence_text( record_id, sentence_id),
    FOREIGN KEY ( semantic ) REFERENCES concept_semantic( semantic_id ),
    FOREIGN KEY ( assertion ) REFERENCES concept_assertion( assertion_id ),
    PRIMARY KEY ( concept_id )
);

Create relation_type table:
CREATE table relation_type (
    relation_type_id tinyint UNSIGNED AUTO_INCREMENT,
    relation_semantic_type varchar(15),
    relation_type varchar(15),
    PRIMARY KEY ( relation_type_id )
);

Create relations table:
CREATE table relations (
    relation_id mediumint UNSIGNED AUTO_INCREMENT,
    record_id mediumint UNSIGNED,
    sentence_id smallint UNSIGNED,
    concept_id mediumint UNSIGNED,
    relation_type_id tinyint UNSIGNED,
    c_start smallint UNSIGNED,
    c_end smallint UNSIGNED,
    relation_text text,
    FOREIGN KEY ( record_id, sentence_id ) REFERENCES sentence_text( record_id, sentence_id ),
    FOREIGN KEY ( concept_id ) REFERENCES concepts( concept_id ),
    FOREIGN KEY ( relation_type_id ) REFERENCES relation_type( relation_type_id ),
    PRIMARY KEY ( relation_id )
);
