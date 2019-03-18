package oauth;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class GoodReadsSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;
    private static final Logger log = LoggerFactory.getLogger(GoodReadsSpeechletRequestStreamHandler.class);
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.ask.skill.5d7de6eb-37c4-423b-b245-7892847f15e9");
    }

    public GoodReadsSpeechletRequestStreamHandler() {
        super(new GoodReadsSpeechlet(), supportedApplicationIds);
    }
}
