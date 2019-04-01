package conditions;

import parser.ParsingUtils;

public class SentenceCondition implements ConditionIntf {

    // Data
    int mRecordId;
    int mSentenceId;
    String mSection;
    int mIndex1;
    int mIndex2;

    String mSQLQuery;
    boolean mParsingErrorOccurred;

    // Methods

    public SentenceCondition( String[] aParts, int aRecordId, int aSentenceId ) {
        parseParts( aParts, aRecordId, aSentenceId );
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
            return "";
        }

        return "mysqlquery";
    }
}