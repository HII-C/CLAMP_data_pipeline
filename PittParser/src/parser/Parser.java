package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

import conditions.*;

public class Parser {
    public String mFileName = "";
    public int mSentenceId = 0;

    public static void main(String[] args) {
    	Parser parse = new Parser();

    	String workingDirectory = System.getProperty("user.dir");
    	System.out.println( "Current working directory: " + workingDirectory );

        //parse.mFileName = args[1];
        parse.mFileName = "PittParser\\example_data";
        parse.readFileByLine();
    }

    public void readFileByLine( ) {
        String theLine = null;

        try {
            FileReader fileReader = new FileReader( mFileName );
            BufferedReader bufferedReader = new BufferedReader( fileReader );

            while((theLine = bufferedReader.readLine()) != null ) {
                if( theLine.equals("") ) continue;

                ConditionIntf theCondition = getConditionFromLine( theLine );
                if( theCondition == null ) continue;
                
                String theSQLQuery = theCondition.getSQLAddQuery();
                if( theSQLQuery.equals( "" ) ) continue;

                // TODO: run this sql query
            }

            bufferedReader.close();
        }
        catch( Exception e ) {
            System.out.println("File Read error: " + mFileName);
        }
    }

    public ConditionIntf getConditionFromLine( String line ) {
        String[] theSplitLine = ParsingUtils.splitByMinSpace( line, 3 );
        int theRecordId = 1;

        System.out.println(" The parse line is: " + Arrays.toString( theSplitLine ));

        String theConditionType = theSplitLine[0];
        ConditionIntf theResCondition = null;
        if( theConditionType.equals("NamedEntity") ) {
            theResCondition = new ConceptCondition(theSplitLine, theRecordId);
        } else if( theConditionType.equals("Sentence") ) {
            theResCondition = new SentenceCondition(theSplitLine, theRecordId, mSentenceId);
            mSentenceId++;
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