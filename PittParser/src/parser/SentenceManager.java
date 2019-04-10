package parser;

import java.util.ArrayList;
import java.util.List;

public class SentenceManager {

    // list of sentence ranges put in order of sentence ID
    List<int[]> sentenceRanges;

    public SentenceManager() {
        sentenceRanges = new ArrayList<int[]>();
    }

    public Integer retrieveSentenceRangeMatch(int startIndex, int endIndex) {
        Integer resIndex = null;

        for(int i = 0 ; i < sentenceRanges.size(); i++) {
            int[] currRange = sentenceRanges.get(i);

            boolean startIndexValid = startIndex >= currRange[0] && startIndex <= currRange[1];
            boolean endIndexValid = endIndex >= currRange[0] && endIndex <= currRange[1];

            if( (startIndexValid && !endIndexValid) || (!startIndexValid && endIndexValid)) {
                System.out.println("Sentence Manager Error: Entity may be a part of more than one index range");
                return null;
            }

            if( startIndexValid && endIndexValid ) {
                resIndex = i;
            }
        }

        return resIndex;
    }

    public void addSentenceRange( int[] sentenceRange ) {
        sentenceRanges.add( sentenceRange );
    }
}
