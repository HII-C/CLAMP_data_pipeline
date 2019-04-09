package conditions;

import parser.ParsingUtils;
import parser.SentenceManager;

import java.util.ArrayList;
import java.util.List;

public class RelationCondition implements ConditionIntf {

    // Data
    int mRecordId;
    int mLeftIndex1;
    int mLeftIndex2;
    int mRightIndex1;
    int mRightIndex2;
    String mRelationType;
    String mSemanticType1;
    String mSemanticType2;

    int mSentenceID1;
    int mSentenceID2;

    boolean mParsingErrorOccurred;
    boolean mHasSentenceIDSet;

    public RelationCondition( String[] aParts, int aRecordId ) {
        parseParts(aParts,aRecordId);
    }

    // Expected input
    // e.g. Relation        175     187     problem 172     174     NEG     semantic=NEG_Of
    // Note that "problem 172" might be grouped since there's only one space to delimit the two
    private void parseParts(String[] aParts, int aRecordId) {
        if( !aParts[0].equals("Relation") ) {
            printError("Type is incorrect: " + aParts[0]);
            return;
        }

        if( aParts.length != 7 ) {
            printError( "Unexpected parts size: " + aParts.length );
            return;
        }

        mRecordId = aRecordId;

        Integer theIndex1 = ParsingUtils.parseInt( aParts[1] );
        Integer theIndex2 = ParsingUtils.parseInt( aParts[2] );

        if( theIndex1 == null || theIndex2 == null ) {
            printError( "Index parsing error: " + aParts[1] + " - " + aParts[2] );
            return;
        }

        mLeftIndex1 = theIndex1;
        mLeftIndex2 = theIndex2;

        // handle the "problem 172" grouped case here:
        String[] problemSplit = aParts[3].split(" ");
        if( problemSplit.length != 2 ) {
            printError( "Problem parsing problem split: " + aParts[3]);
            return;
        }

        mRelationType = problemSplit[0];

        theIndex1 = ParsingUtils.parseInt( problemSplit[1] );
        theIndex2 = ParsingUtils.parseInt( aParts[4] );

        if( theIndex1 == null || theIndex2 == null ) {
            printError( "Index parsing error: " + problemSplit[1] + " - " + aParts[4] );
            return;
        }

        mRightIndex1 = theIndex1;
        mRightIndex2 = theIndex2;

        mSemanticType1 = aParts[5];
        mSemanticType2 = ParsingUtils.splitRight(aParts[6], "semantic=");
        if( mSemanticType2.equals("")) {
            printError("Semantic Type 2 parsing error: " + aParts[6]);
            return;
        }
    }

    private void printError(String errorMessage) {
        mParsingErrorOccurred = true;
        System.out.println("RID: " + mRecordId + " - Relation Condition - " + errorMessage);
    }

    // ConditionIntf OVERRIDES

    @Override
    public List<String> getSQLAddQuery() {
        List<String> theSQLQueries = new ArrayList<String>();

        if( mParsingErrorOccurred || !mHasSentenceIDSet ) {
            printError( "Requirements not satisfied for SQL query or error occurred");
            return theSQLQueries;
        }

        return theSQLQueries;
    }

    @Override
    public void updateSentenceID( SentenceManager aSentenceManager ) {
        Integer leftSentenceID = aSentenceManager.retrieveSentenceRangeMatch(mLeftIndex1, mLeftIndex2);
        Integer rightSentenceID = aSentenceManager.retrieveSentenceRangeMatch(mRightIndex1, mRightIndex2);

        if( leftSentenceID == null || rightSentenceID == null || mParsingErrorOccurred ) {
            printError( "Error Updating Sentence ID");
            return;
        }

        mSentenceID1 = leftSentenceID;
        mSentenceID2 = rightSentenceID;

        mHasSentenceIDSet = true;
    }

	@Override
	public void updateSentenceText() {
		// no-op
	}
}
