package conditions;

import parser.ParsingUtils;
import parser.SentenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConceptCondition implements ConditionIntf{

    String[] mParts;

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
    boolean mHasFailedFromTemporal;

    // Methods

    public ConceptCondition(String[] aParts, int aRecordId) {
        parseParts(aParts, aRecordId);
    }

    // Expected input
    // e.g. NamedEntity     42      62      semantic=problem        assertion=present      ne=Difficulty breathing
    private void parseParts(String[] aParts, int aRecordId) {
        mParts = aParts;

        // validate that this entry is correct
        if( !aParts[0].equals("NamedEntity") ) {
            printError("Type is incorrect: " + aParts[0]);
            return;
        }

        if( aParts.length != 7 && aParts.length != 6 ) {
            printError("Unexpected parts size: " + aParts.length);
            return;
        }

        // Some records have assertion, and some don't~???
        boolean hasAssertion = aParts.length == 7;

        mRecordId = aRecordId;

        Integer theIndex1 = ParsingUtils.parseInt( aParts[1] );
        Integer theIndex2 = ParsingUtils.parseInt( aParts[2] );

        if( theIndex1 == null || theIndex2 == null ) {
            printError( "Index parsing error: " + aParts[1] + " - " + aParts[2] );
            return;
        }

        mIndex1 = theIndex1;
        mIndex2 = theIndex2;

        String theSemanticString = ParsingUtils.splitRight( aParts[3], "semantic=");
        if( theSemanticString.equals("") ){
            printError( "Semantic parsing error: " + aParts[3] );
            return;
        }

        if( theSemanticString.equals("temporal")) {
            mHasFailedFromTemporal = true;
            printError( "Ignoring temporal Concepts! " );
            return;
        }

        mSemantic = theSemanticString;

        int theIndex = 4;

        if( hasAssertion ) {
            String theAssertionString = ParsingUtils.splitRight( aParts[theIndex], "assertion=");
            if( theAssertionString.equals("") ){
                printError( "Assertion parsing error: " + aParts[theIndex] );
                return;
            }
            mAssertion = theAssertionString;
            theIndex++;
        }

        mConceptUID = ParsingUtils.splitRight( aParts[theIndex], "cui=" );
        if( mConceptUID.equals("") ){
            printError( "Concept UID parsing error: " + aParts[theIndex] );
            return;
        }
        theIndex++;


        String theTextString = ParsingUtils.splitRight( aParts[theIndex], "ne=" );
        if( theTextString.equals("") ) {
            printError( "NE parsing error: " + aParts[theIndex] );
            return;
        }

        mText = theTextString;
        theIndex++;

        // success!
    }

    private void printError(String errorMessage) {
        mParsingErrorOccurred = true;

        // suppress output if a result of temporal concept
        if( !mHasFailedFromTemporal ) {
            System.out.println("RID: " + mRecordId + " - Concepts Condition - Arguments: " + Arrays.toString( mParts ) + " - " +  errorMessage);
        }
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

        String conceptQuery;
        if( mAssertion != null ) {
            conceptQuery =              "INSERT INTO concepts ( record_id, sentence_id, cui, c_start, c_end, text, semantic, assertion ) " +
                                        "SELECT " + mRecordId + "," + mSentenceId + ",'" + mConceptUID + "'," + mIndex1 + "," + mIndex2 + ",'" + mText + "', concept_semantic.semantic_id, concept_assertion.assertion_id " +
                                        "FROM concept_semantic, concept_assertion " +
                                        " WHERE concept_semantic.semantic_id= '" + mSemantic + "' " +
                                        "AND concept_assertion.assertion_id = '" + mAssertion + "';";

            String conceptAssertionQuery =  "INSERT INTO concept_assertion ( assertion_text ) " +
                                            "SELECT '" + mAssertion + "' " +
                                            "WHERE NOT EXISTS ( SELECT * FROM concept_assertion " +
                                            "WHERE concept_assertion.assertion_text = '" + mAssertion + "');";
            theSQLQueries.add( conceptAssertionQuery );

        }
        else {
            conceptQuery =              "INSERT INTO concepts ( record_id, sentence_id, cui, c_start, c_end, text, semantic ) " +
                                        "SELECT " + mRecordId + "," + mSentenceId + ",'" + mConceptUID + "'," + mIndex1 + "," + mIndex2 + ",'" + mText + "', concept_semantic.semantic_id " +
                                        "FROM concept_semantic " +
                                        " WHERE concept_semantic.semantic_id= '" + mSemantic + "';";
        }

        theSQLQueries.add( conceptSemanticQuery );
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

    @Override
    public boolean hasSQLGenerationCompletedSuccessfully(){
        return mHasFailedFromTemporal || ( !mParsingErrorOccurred && mHasSentenceIDSet );
    }
}