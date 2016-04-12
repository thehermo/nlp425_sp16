package edu.pugetsound.mathcs.nlp.processactions;

import java.lang.Thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;


import edu.pugetsound.mathcs.nlp.lang.Utterance;
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.datag.DAClassifier;
// import edu.pugetsound.mathcs.nlp.processactions.ExtendedDialogueActTag;
// import edu.pugetsound.mathcs.nlp.processactions.srt.*;


//Requires Jython 2.5: http://www.jython.org/
//http://search.maven.org/remotecontent?filepath=org/python/jython-standalone/2.7.0/jython-standalone-2.7.0.jar
import org.python.util.PythonInterpreter;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PyInteger;


/**
 * The main response generator of the Process Actions step
 * This class should only be used to access the method generateResponse(...);
 * @author Thomas Gagne
 */
public class ResponseGenerator {


    private static String[] getParagraphs(File f) throws FileNotFoundException,IOException {
        FileReader fr = new FileReader(f);
        BufferedReader bufferedReader = new BufferedReader(fr);
        ArrayList<String> paragraphs = new ArrayList<String>();
        String paragraph = "";
        String line;
        ArrayList<Character> endPunct = new ArrayList<Character>() {{ 
            add('.'); 
            add('!'); 
            add('"'); 
            add('\''); 
            add('?'); 
            add('!'); 
        }};

        while( (line = bufferedReader.readLine()) != null){
            if (line.length() > 1)
                paragraph += line.trim()+" ";
            if (endPunct.contains(new Character(paragraph.charAt(paragraph.length()-2))) || (line.length() <= 1 && paragraph.length() > 1)) {
                paragraphs.add(new String(paragraph));
                paragraph = "";
            }}
        bufferedReader.close();
        String[] prs = new String[paragraphs.size()];
        return paragraphs.toArray(prs);
    }



    public static void main(String a[]) {
        ArrayList<String> args = new ArrayList<String>(Arrays.asList(a));
        if (args.contains("-h") || args.contains("--help")) {
            System.out.println( "This script will read a text file of utterances and generate"
                +" responses from them for the processActions templates to use."
                +"Now, we can make this bot talk like Abe Lincoln or Darth Vader or Donald Trump!!!"
                +"Make NLP great again!\n\n"
                +"Usage:\n\tjava -cp ClasspathToJars edu.pugetsound.mathcs."
                +"nlp.processactions.ResponseGenerator args inputFile.txt outputFile.txt\n"
                +"inputFile.txt: The exact path to the file to be used as input\n"
                +"outputFile.txt: The name of the file to be writen to as output, within the "
                +"processactions/srt folder. Defaults to responses.json. If file exists, it is read "
                +"from and responses are added to it.\n"
                +"Args:\n\t-h, --help: Display this message" );
        } 
        else if (args.size() > 2 || args.size() == 0){
            System.out.println("Error with arguments: need one or two. You provided "+args.size());
        } 
        else {
            File inputFile = new File(args.get(0));
            if(!inputFile.exists() || inputFile.isDirectory()) {
                System.out.println("Error with first arg: not a valid file");
            } 
            else {
                if (args.size() == 1)
                    args.add("responses.json");

                try {

                    System.out.println("Reading from input file at "+inputFile.getAbsolutePath());
                    String[] paragraphs = getParagraphs(inputFile);
                    System.out.println("Done reading from input file.");

                    String paragraph;
                    long queryTime = 0;
                    Conversation utterances;
                    PyString[] daTags;
                    PyList texts;
                    List<Utterance> convo;
                    int numTokens, utterancesLen, amrLen, daTagsLen;
                    DAClassifier classifier = new DAClassifier();
                    PythonInterpreter python = new PythonInterpreter();
                    python.execfile("../scripts/responseTemplater.py");
                    python.set("fn", new PyString("../src/edu/pugetsound/mathcs/nlp/processactions/srt/" + args.get(1)));
                        
                    for (int p=0; p<paragraphs.length; p++) {
                        try {
                            python.exec("utterancesLen = len(utterances)");
                            utterancesLen = ((PyInteger) python.get("utterancesLen")).asInt();
                            python.exec("amrLen = len(AMRs)");
                            amrLen = ((PyInteger) python.get("amrLen")).asInt();
                            python.exec("daTagsLen = len(DATags)");
                            daTagsLen = ((PyInteger) python.get("daTagsLen")).asInt();
                            if (utterancesLen != amrLen)
                                if (utterancesLen > amrLen) {
                                    python.exec("utterances = utterances[:amrLen]");
                                    python.exec("utterancesLen = len(utterances)");
                                    utterancesLen = ((PyInteger) python.get("utterancesLen")).asInt();
                                }
                                else {
                                    python.exec("AMRs = AMRs[:utterancesLen]");
                                    python.exec("amrLen = len(AMRs)");
                                    amrLen = ((PyInteger) python.get("amrLen")).asInt();
                                }
                            if (utterancesLen > daTagsLen){
                                python.exec("utterances = utterances[:daTagsLen]");
                                python.exec("utterancesLen = len(utterances)");
                                utterancesLen = ((PyInteger) python.get("utterancesLen")).asInt();
                            }
                            if (amrLen > daTagsLen){
                                python.exec("AMRs = AMRs[:daTagsLen]");
                                python.exec("amrLen = len(AMRs)");
                                amrLen = ((PyInteger) python.get("amrLen")).asInt();
                            }

                            if (p > 15 && (p % 15) == 0){
                                System.out.println("Writing current progress to file since we've analyzed "+p+" paragraphs and "+utterancesLen+" utterances.");
                                python.exec("main(fn)");
                            }


                            paragraph = paragraphs[p];
                            System.out.println("Now querying MSR SPLAT for AMR/Tokens for paragraph "+(p+1)+" of "+paragraphs.length);
                            
                            python.set("text", new PyString(paragraph)); //Watch out for "Cannot create PyString with non-byte value"
                            if (System.currentTimeMillis() - queryTime < 1500)
                                Thread.sleep(1500 - System.currentTimeMillis() + queryTime);
                            python.exec("numTokens = analyzeUtteranceString(text)");
                            queryTime = System.currentTimeMillis();
                            numTokens = ((PyInteger) python.get("numTokens")).asInt();
                            System.out.println("Done querying MSR SPLAT for AMR/Tokens - got "+numTokens+" new ones!\n"
                                +"Now asking the DAClassifier to classify each utterance with a DATag for paragraph "+(p+1)+" of "+paragraphs.length);

                            utterances = new Conversation();
                            texts = (PyList) ((PyList) python.get("utterances")).subList(utterancesLen, utterancesLen+numTokens);
                            for (Object utt: texts) 
                                utterances.addUtterance(new Utterance((String) utt));
                            daTags = new PyString[numTokens];
                            convo = utterances.getConversation();
                            for (int i=0; i<numTokens; i++) {
                                convo.get(i).daTag = classifier.classify(convo.get(i), utterances);
                                daTags[i] = new PyString(convo.get(i).daTag.toString()); }
                            python.set("lst", new PyList(daTags));
                            python.exec("DATags += lst");
                            System.out.println("Done asking the DAClassifier to classify each utterance with a DATag.");
                            System.out.println("Now have " + (utterancesLen+numTokens) +" utterances total." );
                        } catch(Exception e) {
                            System.out.println("Issue with paragraph "+p+"; reverting any added amrs/utterances/datags to last paragraph.");
                            System.out.println(e);
                        }
                    }
                    python.exec("utterancesLen = len(utterances)");
                    utterancesLen = ((PyInteger) python.get("utterancesLen")).asInt();
                    if (utterancesLen > 0) {
                        System.out.println("Now writing "+utterancesLen+" results to output file at "
                                +"../src/edu/pugetsound/mathcs/nlp/processactions/srt/" + args.get(1));
                        python.exec("main(fn)");
                        System.out.println("Done writing results to output file!");
                    }
                    else {
                        System.out.println("Warning: no utterances/AMRs found. Not writing anything to output file.");
                    }   

                } catch(FileNotFoundException ex) {
                    System.out.println("Error: Unable to open file");    
                    ex.printStackTrace();            
                } catch (IOException e) {
                    System.out.println("Error: IOException");
                    e.printStackTrace();
                } 
            }
        }
    }       

}
