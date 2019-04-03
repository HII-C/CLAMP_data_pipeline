package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import conditions.*;

public class Parser {
    public String mFileName;
    public int mSentenceId = 0;
    List<ConditionIntf> mConditions;

    private SentenceManager mSentenceManager;

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println( "Current working directory: " + workingDirectory );

        //parse.mFileName = args[1];
        Parser parse = new Parser("PittParser\\example_data");

        parse.startParsing();
    }

    public Parser( String aFileName ) {
        mFileName = aFileName;
        mSentenceManager = new SentenceManager();
        mConditions = new ArrayList<ConditionIntf>();
    }

    public void startParsing() {
        parseFileByLine();
        updateConditionsWithSentenceID();
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

            // TODO: RUN QUERY HERE
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