package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import conditions.*;
import mysql_interface.DatabaseManager;
import mysql_interface.DatabaseManagerIntf;

public class Parser {
    public String mFileName;
    private String mUnparsedFileName;
    public int mSentenceId = 0;
    List<ConditionIntf> mConditions;
    private int mRecordId;

    private SentenceManager mSentenceManager;
    private DatabaseManagerIntf mDatabaseManager;

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println( "Current working directory: " + workingDirectory );

        System.out.println( "These are the arguments: " + Arrays.toString( args ) );

        String userName = args[0];
        String password = args[1];

        String theOutputFile = args[2];
        String theInputFile = args[3];

        // FOR TESTING LOCALLY
        //String windows = "PittParser\\pitt_report_67016.txt";
        //String linuxShit = "../pitt_report_67016.txt";

        //String rawFile = "PittParser\\raw.txt";
        //String linuxRaw = "../raw.txt";

        Parser parse = new Parser(theOutputFile, theInputFile);
        parse.setDatabaseCredentials( userName, password );

        parse.startParsing();
    }

    public Parser( String aFileName, String aUnparsedFileName ) {
        mFileName = aFileName;
        mSentenceManager = new SentenceManager();
        mConditions = new ArrayList<ConditionIntf>();
        mDatabaseManager = DatabaseManager.getInstance();
        mUnparsedFileName = aUnparsedFileName;
    }

    public void setDatabaseCredentials( String aUserName, String aPassword ) {
        ((DatabaseManager) mDatabaseManager ).setCredentials( aUserName, aPassword );
    }

    public void startParsing() {
        mDatabaseManager.openConnection();
        mDatabaseManager.pushSqlQuery("USE capstone;");

        parseFileByLine();
        updateConditionsWithSentenceID();
        runSQLQueries();

        mDatabaseManager.closeConnection();
    }

    private void updateConditionsWithSentenceID() {
        for( ConditionIntf condition : mConditions ) {
            condition.updateSentenceID( mSentenceManager );
        }
    }

    private void runSQLQueries() {
        List<ConditionIntf> theOrderedConditions = new ArrayList<>();

        // Other conditions require sentence being set first!
        for( ConditionIntf currCondition : mConditions ) {
            if( currCondition instanceof SentenceCondition ) {
                theOrderedConditions.add( currCondition );
            }
        }

        for( ConditionIntf currCondition : mConditions ) {
            if( !(currCondition instanceof SentenceCondition) ) {
                theOrderedConditions.add( currCondition );
            }
        }

        for( ConditionIntf currCondition : theOrderedConditions ) {
            // If there is an error with the condition, we throw and let the user handle the error!
            if( !currCondition.hasSQLGenerationCompletedSuccessfully() ) {
                System.out.println("Terminating on sample: " + mRecordId);
                System.exit(0);
            }

            List<String> theSQLQuery = currCondition.getSQLAddQuery();

            for( String theQuery : theSQLQuery ) {
                if( theQuery.equals( "" ) ) continue;
                System.out.println( theQuery );

                mDatabaseManager.pushSqlQuery( theQuery );
            }
        }
    }

    private void parseFileByLine( ) {
        String theLine = null;

        Integer theRecord = ParsingUtils.extractRecordFromFileName( mFileName );
        if( theRecord == null ) {
            System.out.println( "Error with the record name parsing!");
            return;
        }

        mRecordId = theRecord;

        try {
            FileReader fileReader = new FileReader( mFileName );
            BufferedReader bufferedReader = new BufferedReader( fileReader );

            while((theLine = bufferedReader.readLine()) != null ) {
                if( theLine.equals("") ) continue;

                ConditionIntf theCondition = getConditionFromLine( theLine );
                if( theCondition == null ) continue;

                mConditions.add( theCondition );
            }

            bufferedReader.close();
        }
        catch( Exception e ) {
            System.out.println("File Read error: " + mFileName);
        }
    }

    private ConditionIntf getConditionFromLine( String line ) {
        String[] theSplitLine =  line.split("\t");

        //System.out.println(" The parse line is: " + Arrays.toString( theSplitLine ));

        String theConditionType = theSplitLine[0];
        ConditionIntf theResCondition = null;
        if( theConditionType.equals("NamedEntity") ) {
            theResCondition = new ConceptCondition(theSplitLine, mRecordId);
        } else if( theConditionType.equals("Sentence") ) {
            theResCondition = new SentenceCondition(theSplitLine, mRecordId, mSentenceId, mUnparsedFileName);

            // unique case for sentences; want to use ranges and update other elements with the correct ranges with the manager
            mSentenceId++;
            SentenceCondition castedSentenceCondition = (SentenceCondition) theResCondition;
            mSentenceManager.addSentenceRange( ((SentenceCondition) theResCondition).getSentenceRange() );
        } else if( theConditionType.equals("Token") ) {
            theResCondition = new TokenCondition(theSplitLine, mRecordId);
        } else if( theConditionType.equals("Relation") ) {
            theResCondition = new RelationCondition(theSplitLine, mRecordId, mUnparsedFileName);
        } else {
            System.out.println( "Unknown condition type: " + theSplitLine[0] );
        }

        return theResCondition;
    }
}