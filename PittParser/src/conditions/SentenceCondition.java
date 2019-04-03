package conditions;

import parser.ParsingUtils;
import parser.SentenceManager;

public class SentenceCondition implements ConditionIntf {

    // Data
    int mRecordId;
    int mSentenceId;
    String mSection;
    Integer mIndex1;
    Integer mIndex2;

    boolean mParsingErrorOccurred;

    // Methods

    public SentenceCondition( String[] aParts, int aRecordId, int aSentenceId ) {
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
    }

    private void printError(String errorMessage) {
        mParsingErrorOccurred = true;
        System.out.println("RID: " + mRecordId + " - Sentence Condition - " + errorMessage);
    }

    // ConditionIntf OVERRIDES

    @Override
    public String getSQLAddQuery() {
        if( mParsingErrorOccurred ) {
            printError( "Requirements not satisfied for SQL query or error occurred");
        }

        return "mysqlquery";
    }

    @Override
    public void updateSentenceID( SentenceManager aSentenceManager ) {
        // no-op
    }
}
