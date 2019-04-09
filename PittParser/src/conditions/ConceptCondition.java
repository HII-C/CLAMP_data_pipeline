package conditions;

import parser.ParsingUtils;
import parser.SentenceManager;

import java.util.ArrayList;
import java.util.List;

public class ConceptCondition implements ConditionIntf{

    // Data
    int mRecordId;
    int mSentenceId;
    String mConceptUID;
    int mIndex1;
    int mIndex2;
    String mText;
    int mType;
    int mAssertion;

    boolean mParsingErrorOccurred;
    boolean mHasSentenceIDSet;

    // Methods

    public ConceptCondition(String[] aParts, int aRecordId) {
        parseParts(aParts, aRecordId);
    }

    // Expected input
    // e.g. NamedEntity     42      62      semantic=problem        assertion=present      ne=Difficulty breathing
    private void parseParts(String[] aParts, int aRecordId) {
        // validate that this entry is correct
        if( !aParts[0].equals("NamedEntity") ) {
            printError("Type is incorrect: " + aParts[0]);
            return;
        }

        if( aParts.length != 7 ) {
            printError("Unexpected parts size: " + aParts.length);
            return;
        }

        mRecordId = aRecordId;

        mConceptUID = ParsingUtils.splitRight( aParts[5], "cui=" );
        if( mConceptUID.equals("") ){
            printError( "Concept UID parsing error: " + aParts[5] );
            return;
        }

        String theAssertionString = ParsingUtils.splitRight( aParts[4], "assertion=");
        if( theAssertionString.equals("") ){
            printError( "Assertion parsing error: " + aParts[4] );
            return;
        }
        mapAssertionToAssertionNumber( theAssertionString );

        String theSemanticString = ParsingUtils.splitRight( aParts[3], "semantic=");
        if( theSemanticString.equals("") ){
            printError( "Semantic parsing error: " + aParts[3] );
            return;
        }
        mapSemanticToTypeNumber( theSemanticString );

        Integer theIndex1 = ParsingUtils.parseInt( aParts[1] );
        Integer theIndex2 = ParsingUtils.parseInt( aParts[2] );

        if( theIndex1 == null || theIndex2 == null ) {
            printError( "Index parsing error: " + aParts[1] + " - " + aParts[2] );
            return;
        }

        mIndex1 = theIndex1;
        mIndex2 = theIndex2;

        String theTextString = ParsingUtils.splitRight( aParts[6], "ne=" );
        if( theTextString.equals("") ) {
            printError( "NE parsing error: " + aParts[6] );
            return;
        }

        mText = theTextString;

        // success!
    }

    private void mapSemanticToTypeNumber(String aSemantic) {
        if( aSemantic.equals("problem") ) {
            mType = 1;
        } else if( aSemantic.equals("test") ) {
            mType = 2;
        } else if( aSemantic.equals("treatment") ) {
            mType = 3;
        } else {
            mType = -1;
            printError("Semantic mapping invalid: " + aSemantic);
        }
    }

    private void mapAssertionToAssertionNumber(String aAssertion) {
        if( aAssertion.equals("absent") ){
            mAssertion = 1;
        } else if( aAssertion.equals("hypothetical") ){
            mAssertion = 2;
        } else if( aAssertion.equals("present") ){
            mAssertion = 3;
        } else {
            mAssertion = -1;
            printError("Assertion mapping invalid: " + aAssertion);
        }
    }

    private void printError(String errorMessage) {
        mParsingErrorOccurred = true;
        System.out.println("RID: " + mRecordId + " - Concepts Condition - " + errorMessage);
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
        Integer sentenceID = aSentenceManager.retrieveSentenceRangeMatch(mIndex1, mIndex2);
        if( sentenceID == null || mParsingErrorOccurred ) {
            printError( "Error Updating Sentence ID");
            return;
        }

        mSentenceId = sentenceID;
        mHasSentenceIDSet = true;
    }
}