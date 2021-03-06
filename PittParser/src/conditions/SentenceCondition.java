package conditions;

import java.io.BufferedReader;
import java.io.FileReader;

import parser.ParsingUtils;
import parser.SentenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SentenceCondition implements ConditionIntf {

    String[] mParts;

    // Data
    int mRecordId;
    int mSentenceId;
    String mSection;
    Integer mIndex1;
    Integer mIndex2;
    String mSentenceText;
    String mUnparsedFileName;

    boolean mParsingErrorOccurred;
    boolean mHasSetSentenceText;

    // Methods

    public SentenceCondition( String[] aParts, int aRecordId, int aSentenceId, String aUnparsedFileName ) {
        mParts = aParts;

        mUnparsedFileName = aUnparsedFileName;
        parseParts( aParts, aRecordId, aSentenceId );
    }

    public int[] getSentenceRange() {
        if( mIndex1 == null || mIndex2 == null ) {
            return null;
        }

        int[] res = {mIndex1, mIndex2};
        return res;
    }
    
    private void parseParts( String[] aParts, int aRecordId, int aSentenceId ) {
        if( !aParts[0].equals("Sentence") ) {
            printError( "Type is incorrect: " + aParts[0]);
            return;
        }

        if( aParts.length != 4 ) {
            printError("Unexpected parts size: " + aParts.length);
            return;
        }

        mRecordId = aRecordId;
        mSentenceId = aSentenceId;

        mSection = ParsingUtils.splitRight( aParts[3], "section=" );
        if( mSection.equals("") ) {
            printError( "Section parsing error: " + aParts[3] );
            return;
        }

        Integer lIndex = ParsingUtils.parseInt(aParts[1]);
        Integer rIndex = ParsingUtils.parseInt(aParts[2]);

        if( lIndex == null || rIndex == null ) {
            printError("Index parsing error: " + aParts[1] + " - " + aParts[2]);
            return;
        }

        mIndex1 = lIndex;
        mIndex2 = rIndex;
        
        if( !updateSentenceText() ) {
            printError( "Error getting sentence text");
            return;
        }
    }

    public boolean updateSentenceText() {
        String sentence = "";
        try {
            FileReader fileReader = new FileReader( mUnparsedFileName );
            BufferedReader bufferedReader = new BufferedReader( fileReader );
            int count = 0;
            int intChar;

            while (count < mIndex1) {
                bufferedReader.read();
                count++;
            }

            while ( (intChar = bufferedReader.read()) != -1 && count < mIndex2) {
                char ch = (char) intChar;
                if ( ch == '\n' ) {
                    sentence += " ";
                } else {
                    sentence += ch;
                }
                count++;
            }

            bufferedReader.close();
        }
        catch ( Exception e ) {
            return false;
        }

        mSentenceText = ParsingUtils.cleanInput( sentence );
        mHasSetSentenceText = true;
        return true;
    }

    private void printError(String errorMessage) {
        mParsingErrorOccurred = true;
        System.out.println("RID: " + mRecordId + " - Sentence Condition - Arguments: " + Arrays.toString( mParts ) + " - " + errorMessage);
    }

    // ConditionIntf OVERRIDES

    @Override
    public List<String> getSQLAddQuery() {
        List<String> theSQLQueries = new ArrayList<String>();

        if( mParsingErrorOccurred ) {
            printError( "Requirements not satisfied for SQL query or error occurred");
            return theSQLQueries;
        }

        String sentenceSectionQuery =   "INSERT INTO sentence_section ( section_text ) " +
                                        "SELECT '" + mSection + "' " +
                                        "WHERE NOT EXISTS ( SELECT * FROM sentence_section " +
                                        "WHERE sentence_section.section_text = '" + mSection + "');";

        String sentenceIndexQuery =     "INSERT INTO sentence_index " +
                                        "( record_id, sentence_id, section_id, c_start, c_end ) " +
                                        "SELECT " + mRecordId + "," + mSentenceId + ", sentence_section.section_id, " + mIndex1 + "," + mIndex2 + " " +
                                        "FROM sentence_section " +
                                        " WHERE sentence_section.section_text= '" + mSection + "';";

        String sentenceTextQuery =      "INSERT INTO sentence_text " +
                                        "( record_id, sentence_id, section, sentence ) " +
                                        "SELECT " + mRecordId + "," + mSentenceId + ", sentence_section.section_id, '" + mSentenceText + "' " +
                                        "FROM sentence_section " +
                                        " WHERE sentence_section.section_text= '" + mSection + "';";

        theSQLQueries.add( sentenceSectionQuery );
        theSQLQueries.add( sentenceIndexQuery );
        theSQLQueries.add( sentenceTextQuery );

        return theSQLQueries;
    }

    @Override
    public void updateSentenceID( SentenceManager aSentenceManager ) {
        // no-op
    }

    @Override
    public boolean hasSQLGenerationCompletedSuccessfully(){
        return (!mParsingErrorOccurred);
    }
}
