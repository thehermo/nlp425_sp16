package edu.pugetsound.mathcs.nlp.processactions;

import edu.pugetsound.mathcs.nlp.features.TextAnalyzer;
import edu.pugetsound.mathcs.nlp.kb.KBController;
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;

import java.io.IOException;

/**
 * The main response generator of the Process Actions step
 * This class should only be used to access the method generateResponse(...);
 * Every method of this class is static
 * @author Thomas Gagne & Jon Sims
 * @version 04/26/16
 */
public class ActionProcessor {
	
	private static KBController kb = new KBController("knowledge/cats.pl");
 
    /**
     * Wrapper function that converts an utterance to a conversation
     * For backwards compatability only; use the one that takes a conversation preferably!
     * @return A string representation of the response. In early versions, this might be an AMR
     */
    public static String generateResponse(Utterance utterance, ResponseTag responseDATag) {
        Conversation convo = new Conversation();
        convo.addUtterance(utterance);
        return generateResponse(convo, responseDATag);
    }

    /**
     * Takes in a conversation and a DA tag for what type of statement to respond from the MDP
     * Returns a string corresponding to the generated response
     * @param convo The conversation thus far, so we can use local info to generate the response
     * @param responseTag The type of response we should respond with. Ex: YesNoAnswer
     * @return A string representation of the response. In early versions, this might be an AMR
     */
    public static String generateResponse(Conversation convo, ResponseTag responseTag) {

    	switch(responseTag){
    		case CONVENTIONAL_OPENING : 
    			return "Hello";
    			
    		case CONVENTIONAL_CLOSING : 
    			return "Goodbye";
    			
    		case APOLOGY :
    			return "I'm sorry";
    			
    		case THANKS :
    			return "Thank you";
    			
    		case WELCOME :
    			return "You're welcome";
    		
    		case YES_NO_ANSWER :
    			Utterance utterance = convo.getLastUtterance();
    			if(utterance != null && utterance.firstOrderRep != null){
    				System.out.println(utterance.firstOrderRep);
    				if(kb.yesNo(utterance.firstOrderRep)){
    					return "Yes";
    				}
    				else{
    					return "No";
    				}
    			}
    		default:
    			return "Statement";
    	}
    }

    /**
     * Generates a list of responses to an inputted conversation.
     * @param args A list of Strings should be given, each being 1 line of user input in the convo
     */
    public static void main(String[] args) throws IOException {
        TextAnalyzer ta = new TextAnalyzer();
        Conversation convo = new Conversation();
        for (String a: args) {
            convo.addUtterance(ta.analyze(a,convo));
        }
        for (Utterance utt: convo.getConversation()){
           // System.out.println(generateResponse(utt, ResponseTag.GREETING));
        }
    }

}
