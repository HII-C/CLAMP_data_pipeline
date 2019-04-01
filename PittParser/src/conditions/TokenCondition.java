package conditions;

import parser.ParsingUtils;

public class TokenCondition implements ConditionIntf{

    // Data
    int mRecordId;
    int mSentenceId;
    int mCStart;
    int mCEnd;
    String pos;

    String mSQLQuery;
    boolean mParsingErrorOccurred;

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
        if( mParsingErrorOccurred ) {
            return "";
        }

        return "mysqlquery";
    }
}