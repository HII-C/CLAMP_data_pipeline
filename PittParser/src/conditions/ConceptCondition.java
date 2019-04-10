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
    String mSemantic;
    String mAssertion;

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
        mAssertion = theAssertionString;

        String theSemanticString = ParsingUtils.splitRight( aParts[3], "semantic=");
        if( theSemanticString.equals("") ){
            printError( "Semantic parsing error: " + aParts[3] );
            return;
        }

        mSemantic = theSemanticString;

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

        String conceptSemanticQuery =   "INSERT INTO concept_semantic ( semantic_text ) " +
                                        "SELECT '" + mSemantic + "' " +
                                        "WHERE NOT EXISTS ( SELECT * FROM concept_semantic " +
                                        "WHERE concept_semantic.semantic_text = '" + mSemantic + "');";

        String conceptAssertionQuery =  "INSERT INTO concept_assertion ( assertion_text ) " +
                                        "SELECT '" + mAssertion + "' " +
                                        "WHERE NOT EXISTS ( SELECT * FROM concept_assertion " +
                                        "WHERE concept_assertion.assertion_text = '" + mAssertion + "');";

        String conceptQuery =           "INSERT INTO concepts ( record_id, sentence_id, cui, c_start, c_end, text, semantic, assertion ) " +
                                        "SELECT " + mRecordId + "," + mSentenceId + ",'" + mConceptUID + "'," + mIndex1 + "," + mIndex2 + ",'" + mText + "', concept_semantic.semantic_id, concept_assertion.assertion_id " +
                                        "FROM concept_semantic, concept_assertion " +
                                        " WHERE concept_semantic.semantic_id= '" + mSemantic + "' " +
                                        "AND concept_assertion.assertion_id = '" + mAssertion + "';";

        theSQLQueries.add( conceptSemanticQuery );
        theSQLQueries.add( conceptAssertionQuery );
        theSQLQueries.add( conceptQuery );

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