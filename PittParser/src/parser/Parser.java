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
    public int mSentenceId = 0;
    List<ConditionIntf> mConditions;

    private SentenceManager mSentenceManager;
    private DatabaseManagerIntf mDatabaseManager;

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println( "Current working directory: " + workingDirectory );

        System.out.println( "These are the arguments: " + Arrays.toString( args ) );

        String userName = args[0];
        String password = args[1];

        Parser parse = new Parser("PittParser\\example_data");
        parse.setDatabaseCredentials( userName, password );

        parse.startParsing();
    }

    public Parser( String aFileName ) {
        mFileName = aFileName;
        mSentenceManager = new SentenceManager();
        mConditions = new ArrayList<ConditionIntf>();
        mDatabaseManager = DatabaseManager.getInstance();
    }

    public void setDatabaseCredentials( String aUserName, String aPassword ) {
        ((DatabaseManager) mDatabaseManager ).setCredentials( aUserName, aPassword );
    }

    public void startParsing() {
        mDatabaseManager.openConnection();

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
        for( ConditionIntf currCondition : mConditions ) {
            String theSQLQuery = currCondition.getSQLAddQuery();
            if( theSQLQuery.equals( "" ) ) continue;

            mDatabaseManager.pushSqlQuery( theSQLQuery );
        }
    }

    private void parseFileByLine( ) {
        String theLine = null;

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
        String[] theSplitLine = ParsingUtils.splitByMinSpace( line, 3 );
        int theRecordId = 1;

        System.out.println(" The parse line is: " + Arrays.toString( theSplitLine ));

        String theConditionType = theSplitLine[0];
        ConditionIntf theResCondition = null;
        if( theConditionType.equals("NamedEntity") ) {
            theResCondition = new ConceptCondition(theSplitLine, theRecordId);
        } else if( theConditionType.equals("Sentence") ) {
            theResCondition = new SentenceCondition(theSplitLine, theRecordId, mSentenceId);

            // unique case for sentences; want to use ranges and update other elements with the correct ranges with the manager
            mSentenceId++;
            SentenceCondition castedSentenceCondition = (SentenceCondition) theResCondition;
            mSentenceManager.addSentenceRange( ((SentenceCondition) theResCondition).getSentenceRange() );
        } else if( theConditionType.equals("Token") ) {
            theResCondition = new TokenCondition(theSplitLine, theRecordId);
        } else if( theConditionType.equals("Relation") ) {
            theResCondition = new RelationCondition(theSplitLine, theRecordId);
        } else {
            System.out.println( "Unknown condition type: " + theSplitLine[0] );
        }

        return theResCondition;
    }
}