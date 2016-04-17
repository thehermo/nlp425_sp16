package edu.pugetsound.mathcs.nlp.processactions.srt;

import java.util.Random;
import java.util.HashMap;
import java.util.List;


import edu.pugetsound.mathcs.nlp.lang.Utterance;
import edu.pugetsound.mathcs.nlp.lang.Conversation;

import edu.pugetsound.mathcs.nlp.datag.DialogueActTag;
import edu.pugetsound.mathcs.nlp.lang.AMR;
import edu.pugetsound.mathcs.nlp.processactions.srt.SemanticResponseTemplate;
import edu.pugetsound.mathcs.nlp.processactions.srt.QuestionTemplate;

/**
 * @author Thomas Gagne & Jon Sims
 * A template for constructing wh-questions.
 * This class will first attempt to ask something about what the user said, but if it fails to do so
 * it will try to ask a general question about whatever.
 */
public class WhQuestionTemplate implements SemanticResponseTemplate {

    @Override
    public String constructResponseFromTemplate(Conversation convo) {
        Utterance utterance = convo.getLastUtterance();
        return new QuestionTemplate().constructResponseFromTemplate(convo);
    }

}

