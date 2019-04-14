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

        if( aParts.length != 7 && aParts.length != 6 && aParts.length != 5 ) {
            printError("Unexpected parts size: " + aParts.length);
            return;
        }

        mRecordId = aRecordId;

        Integer theIndex1 = ParsingUtils.parseInt( aParts[1] );
        Integer theIndex2 = ParsingUtils.parseInt( aParts[2] );

        if( theIndex1 == null || theIndex2 == null ) {
            printError( "Index parsing error: " + aParts[1] + " - " + aParts[2] );
            return;
        }

        mIndex1 = theIndex1;
        mIndex2 = theIndex2;

        for( int i = 3; i < aParts.length; i++) {
            parseDependingOnType( aParts[i] );
        }

        // success!
    }

    private void parseDependingOnType( String inputString ) {
        if( inputString.contains( "semantic=" ) ) {

            String theSemanticString = ParsingUtils.splitRight( inputString, "semantic=");
            if( theSemanticString.equals("") ){
                printError( "Semantic parsing error: " + inputString );
                return;
            }

            if( theSemanticString.equals("temporal")) {
                mHasFailedFromTemporal = true;
                printError( "Ignoring temporal Concepts! " );
                return;
            }

            mSemantic = theSemanticString;

        } else if( inputString.contains( "assertion=" ) ) {
            String theAssertionString = ParsingUtils.splitRight( inputString, "assertion=");
            if( theAssertionString.equals("") ){
                printError( "Assertion parsing error: " + inputString );
                return;
            }

            mAssertion = theAssertionString;

        } else if( inputString.contains( "cui=" ) ) {

            mConceptUID = ParsingUtils.splitRight( inputString, "cui=" );
            if( mConceptUID.equals("") ){
                printError( "Concept UID parsing error: " + inputString );
                return;
            }

        } else if( inputString.contains( "ne=" ) ) {

            String theTextString = ParsingUtils.splitRight( inputString, "ne=" );
            if( theTextString.equals("") ) {
                printError( "NE parsing error: " + inputString );
                return;
            }

            mText = theTextString;

        } else {
            printError( "Unknown parsing type: " + inputString);
        }
    }

    private class QueryData {
        public String tableName;
        public String queryData;
        public String queryCondition;

        public String queryForeignQuery;
    }

    private List<QueryData> generateQueryData() {
        List<QueryData> res = new ArrayList<QueryData>();

        if( mSemantic != null ) {
            QueryData newData = new QueryData();
            newData.tableName = "semantic";
            newData.queryData = "concept_semantic.semantic_id";
            newData.queryCondition = "concept_semantic.semantic_id= '" + mSemantic + "' ";

            newData.queryForeignQuery = "INSERT INTO concept_semantic ( semantic_text ) " +
                                        "SELECT '" + mSemantic + "' " +
                                        "WHERE NOT EXISTS ( SELECT * FROM concept_semantic " +
                                        "WHERE concept_semantic.semantic_text = '" + mSemantic + "');";

            res.add( newData );
        }

        if( mAssertion != null ) {
            QueryData newData = new QueryData();
            newData.tableName = "assertion";
            newData.queryData = "concept_assertion.assertion_id";
            newData.queryCondition = "concept_assertion.assertion_id = '" + mAssertion + "' ";

            newData.queryForeignQuery = "INSERT INTO concept_assertion ( assertion_text ) " +
                                        "SELECT '" + mAssertion + "' " +
                                        "WHERE NOT EXISTS ( SELECT * FROM concept_assertion " +
                                        "WHERE concept_assertion.assertion_text = '" + mAssertion + "');";

            res.add( newData );
        }

        if( mConceptUID != null ) {
            QueryData newData = new QueryData();
            newData.tableName = "cui";
            newData.queryData = "'" + mConceptUID + "'";
            newData.queryCondition = "";

            newData.queryForeignQuery = null;

            res.add( newData );
        }

        if( mText != null ) {
            QueryData newData = new QueryData();
            newData.tableName = "text";
            newData.queryData = "'" + ParsingUtils.cleanInput(mText) + "'";
            newData.queryCondition = "";

            newData.queryForeignQuery = null;

            res.add( newData );
        }

        return res;
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

        List<QueryData> theQueryData = generateQueryData();

        // Add the foreign key queries first
        for( QueryData query : theQueryData ) {
            if( query.queryForeignQuery != null ) {
                theSQLQueries.add( query.queryForeignQuery );
            }
        }

        StringBuilder additionalTables = new StringBuilder();
        StringBuilder additionalQueryData = new StringBuilder();
        StringBuilder additionalConditions = new StringBuilder();

        for( int i = 0 ; i < theQueryData.size(); i++) {
            QueryData query = theQueryData.get(i);

            if( i != 0 && !query.queryCondition.equals("") ) {
                additionalConditions.append("AND ");
            }

            additionalTables.append(query.tableName);
            additionalQueryData.append(query.queryData);
            additionalConditions.append(query.queryCondition);

            if( i != theQueryData.size() - 1) {
                additionalTables.append(", ");
                additionalQueryData.append(", ");
            }
        }

        // Now create the big query
        StringBuilder conceptQuery = new StringBuilder();
        conceptQuery.append( "INSERT INTO concepts( record_id, sentence_id, c_start, c_end, " + additionalTables + ") " );
        conceptQuery.append( "SELECT " + mRecordId + ", " + mSentenceId + "," + mIndex1 + ", " + mIndex2 + additionalQueryData + " ");
        conceptQuery.append( "WHERE " + additionalConditions );
        conceptQuery.append( ";" );

        theSQLQueries.add( conceptQuery.toString() );

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