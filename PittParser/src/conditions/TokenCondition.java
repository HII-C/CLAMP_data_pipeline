package conditions;

import parser.ParsingUtils;
import parser.SentenceManager;

public class TokenCondition implements ConditionIntf{

    // Data
    int mRecordId;
    int mSentenceId;
    int mCStart;
    int mCEnd;
    String pos;

    boolean mParsingErrorOccurred;
    boolean mHasSentenceIdSet;

    // ConditionIntf

    public TokenCondition(String[] aParts, int aRecordId) {
        parseParts( aParts, aRecordId );
    }

    private void parseParts(String[] aParts, int aRecordId) {
        if(!aParts[0].equals("Token")){
            printError("Type is incorrect: " + aParts[0]);
            return;
        }

        if( aParts.length != 4 ) {
            printError( "Unexpected parts size: " + aParts.length );
            return;
        }

        mRecordId = aRecordId;

        pos = ParsingUtils.splitRight( aParts[3], "pos=" );
        if( pos.equals("") ) {
            printError("Concept UID parsing error: " + aParts[3] );
            return;
        }

        Integer lIndex = ParsingUtils.parseInt( aParts[1] );
        Integer rIndex = ParsingUtils.parseInt( aParts[2] );

        if( lIndex == null || rIndex == null ) {
            printError( "Index parsing error: " + aParts[1] + " - " + aParts[2] );
            return;
        }

        mCStart = lIndex;
        mCEnd = rIndex;
    }

    private void printError(String errorMessage) {
        mParsingErrorOccurred = true;
        System.out.println("RID: " + mRecordId + " - Token Condition - " + errorMessage);
    }

    @Override
	public String getSQLAddQuery() {
        if( mParsingErrorOccurred || !mHasSentenceIdSet) {
            printError( "Requirements not satisfied for SQL query or error occurred");
        }

        return "mysqlquery";
    }

    @Override
    public void updateSentenceID( SentenceManager aSentenceManager ) {
        Integer sentenceID = aSentenceManager.retrieveSentenceRangeMatch(mCStart, mCEnd);
        if( sentenceID == null || mParsingErrorOccurred ) {
            printError( "Error Updating Sentence ID");
            return;
        }

        mSentenceId = sentenceID;
        mHasSentenceIdSet = true;
    }
}