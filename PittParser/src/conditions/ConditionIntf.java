package conditions;

import parser.SentenceManager;

import java.util.List;

public interface ConditionIntf {
    // returns empty string if there was a parsing problem
    List<String> getSQLAddQuery();

    // Hacky check since we also ignore temporal concepts even though it's technically an error
    boolean hasSQLGenerationCompletedSuccessfully();

    // Updates the condition with the correct sentence ID; should do nothing if the type of the condition is already a sentence
    // NOTE: When this method is called, the SentenceManager MUST already be finished initializing with the total set of sentences.
    void updateSentenceID( SentenceManager aSentenceManager );
}
