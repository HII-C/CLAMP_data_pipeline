package parser;

import java.io.BufferedReader;
import java.io.FileReader;

import conditions.ConceptCondition;
import conditions.ConditionIntf;

public class Parser {
    public String mFileName = "";

    public static void main(String[] args) {
    	Parser parse = new Parser();
    	
        parse.mFileName = args[1];
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
        String[] theSplitLine = ParsingUtils.splitByMinSpace( line, 5 );
        int theRecordId = 1;
        
        String theConditionType = theSplitLine[0];
        ConditionIntf theResCondition = null;
        if( theConditionType.equals("NamedEntity") ) {
            theResCondition = new ConceptCondition(theSplitLine, theRecordId);
        } else if( theConditionType.equals("Sentence") ) {

        } else if( theConditionType.equals("Token") ) {

        } else {
            System.out.println( "Unknown condition type: " + theSplitLine[0] );
        }

        return theResCondition;
    }
}