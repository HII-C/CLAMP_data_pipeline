package conditions;

import parser.ParsingUtils;
import parser.SentenceManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class RelationCondition implements ConditionIntf {

    // Data
    int mRecordId;
    int mTargetIndex1;
    int mTargetIndex2;
    int mRelationIndex1;
    int mRelationIndex2;
    // mTargetType relates to the semantic type of the noun that the relation is targetting
    String mTargetType;
    // mRelationSemanticType relates to the semantic type of the relation, e.g., NEG
    String mRelationSemanticType;
    // mRelationType relates to the type of the relation relative to the target, e.g., NEG_Of
    String mRelationType;
    String mUnparsedFileName;
    
    // mRelationTarget is derived from the text between mTargetIndex1 and mTargetIndex2.
    // 		mRelationTarget is the text identified in the clinical notes that the relation is describing/targetting
    // mRelationText is derived from the text between mRelationIndex1 and mRelationIndex2.
    //		mRelationText is the text identified in the clinical notes that represents the relation
    String mRelationTarget;
    String mRelationText;
    boolean mHasUpdatedRelationText = false;

    int mSentenceID1;
    int mSentenceID2;

    boolean mParsingErrorOccurred;
    boolean mHasSentenceIDSet;

    public RelationCondition( String[] aParts, int aRecordId, String aUnparsedFileName ) {
        mUnparsedFileName = aUnparsedFileName;
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

        if( aParts.length != 8 ) {
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

        mTargetIndex1 = theIndex1;
        mTargetIndex2 = theIndex2;

        mTargetType = aParts[3];

        theIndex1 = ParsingUtils.parseInt( aParts[4] );
        theIndex2 = ParsingUtils.parseInt( aParts[5] );

        if( theIndex1 == null || theIndex2 == null ) {
            printError( "Index parsing error: " + aParts[4] + " - " + aParts[5] );
            return;
        }

        mRelationIndex1 = theIndex1;
        mRelationIndex2 = theIndex2;

        mRelationSemanticType = aParts[6];
        mRelationType = ParsingUtils.splitRight(aParts[7], "semantic=");
        if( mRelationType.equals("")) {
            printError("Semantic Type 2 parsing error: " + aParts[6]);
            return;
        }
        
        mRelationTarget = updateText(mTargetIndex1, mTargetIndex2);
        mRelationText = updateText(mRelationIndex1, mRelationIndex2);
        if (mRelationTarget != "" && mRelationText != "") {
        	mHasUpdatedRelationText = true;
        }
    }

    private String updateText(int mIndex1, int mIndex2) {
        String text = "";
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
                    text += " ";
                } else {
                    text += ch;
                }
                count++;
            }

//    		System.out.println("Sentence: " + sentence);
            bufferedReader.close();
        }
        catch ( Exception e ) {
            System.out.println("File read error: " + mUnparsedFileName);
        }
        
        if (text == "") {
        	printError("Error in extracting text; returned empty string");
        }
        return text;
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
        Integer leftSentenceID = aSentenceManager.retrieveSentenceRangeMatch(mTargetIndex1, mTargetIndex2);
        Integer rightSentenceID = aSentenceManager.retrieveSentenceRangeMatch(mRelationIndex1, mRelationIndex2);

        if( leftSentenceID == null || rightSentenceID == null || mParsingErrorOccurred ) {
            printError( "Error Updating Sentence ID");
            return;
        }

        mSentenceID1 = leftSentenceID;
        mSentenceID2 = rightSentenceID;

        mHasSentenceIDSet = true;
    }

    @Override
    public boolean hasSQLGenerationCompletedSuccessfully(){
        return (!mParsingErrorOccurred && mHasSentenceIDSet);
    }
}
