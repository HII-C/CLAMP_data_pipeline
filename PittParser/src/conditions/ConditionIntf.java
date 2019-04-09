package conditions;

import parser.SentenceManager;

import java.util.List;

public interface ConditionIntf {
    // returns empty string if there was a parsing problem
    List<String> getSQLAddQuery();

    // Updates the condition with the correct sentence ID; should do nothing if the type of the condition is already a sentence
    // NOTE: When this method is called, the SentenceManager MUST already be finished initializing with the total set of sentences.
    void updateSentenceID( SentenceManager aSentenceManager );
}
