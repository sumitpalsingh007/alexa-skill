package oauth;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;


public class GoodReadsSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(GoodReadsSpeechlet.class);
    public static final String BASE_GOODREADS_URL = "https://www.goodreads.com";
    public static final String TOKEN_SERVER_URL = BASE_GOODREADS_URL + "/oauth/request_token";
    public static final String AUTHENTICATE_URL = BASE_GOODREADS_URL + "/oauth/authorize";
    public static final String ACCESS_TOKEN_URL = BASE_GOODREADS_URL + "/oauth/access_token";

    public static final String GOODREADS_KEY = "D9XU6XRd6HKe0JWU5YrIaw";
    public static final String GOODREADS_SECRET = "O3vhWhA6Rs5U5zl77C6lwznKPWuSii8L95Iya3NhuQ";

    @Override
    public void onSessionStarted(final SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", speechletRequestEnvelope.getRequest().getRequestId(),
                speechletRequestEnvelope.getSession().getSessionId());

        //generateGoodReadResponse();
    }

    @Override
    public SpeechletResponse onLaunch(final SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", speechletRequestEnvelope.getRequest().getRequestId(),
                speechletRequestEnvelope.getSession().getSessionId());

        final String speechOutput = "Welcome to Bookster";
        final String repromptText = "What would you like to read";

        // Here we are prompting the user for input
        return newAskResponse(speechOutput, false, repromptText, false);
    }

    @Override
    public SpeechletResponse onIntent(final SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
        final String speechOutput = generateGoodReadResponse();
        final String repromptText = "What would you like to read";

        // Here we are prompting the user for input
        return newAskResponse(speechOutput, false, repromptText, false);
    }

    @Override
    public void onSessionEnded(final SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", speechletRequestEnvelope.getRequest().getRequestId(),
                speechletRequestEnvelope.getSession().getSessionId());
    }

    private String generateGoodReadResponse() {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        // Get Temporary Token
        OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(TOKEN_SERVER_URL);
        signer.clientSharedSecret = GOODREADS_SECRET;
        getTemporaryToken.signer = signer;
        getTemporaryToken.consumerKey = GOODREADS_KEY;
        getTemporaryToken.transport = new NetHttpTransport();
        OAuthCredentialsResponse temporaryTokenResponse = null;
        try {
            temporaryTokenResponse = getTemporaryToken.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build Authenticate URL
        OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(AUTHENTICATE_URL);
        accessTempToken.temporaryToken = temporaryTokenResponse.token;
        String authUrl = accessTempToken.build();

        // Get Access Token using Temporary token and Verifier Code
        OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
        getAccessToken.signer = signer;
        // NOTE: This is the main difference from the StackOverflow example
        signer.tokenSharedSecret = temporaryTokenResponse.tokenSecret;
        getAccessToken.temporaryToken = temporaryTokenResponse.token;
        getAccessToken.transport = new NetHttpTransport();
        getAccessToken.consumerKey = GOODREADS_KEY;
        OAuthCredentialsResponse accessTokenResponse = null;
        try {
            accessTokenResponse = getAccessToken.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build OAuthParameters in order to use them while accessing the resource
        OAuthParameters oauthParameters = new OAuthParameters();
        signer.tokenSharedSecret = accessTokenResponse.tokenSecret;
        oauthParameters.signer = signer;
        oauthParameters.consumerKey = GOODREADS_KEY;
        oauthParameters.token = accessTokenResponse.token;
        oauthParameters.consumerKey = GOODREADS_KEY;

        // Use OAuthParameters to access the desired Resource URL
        HttpRequestFactory requestFactory = new ApacheHttpTransport().createRequestFactory(oauthParameters);
        GenericUrl genericUrl = new GenericUrl("https://www.goodreads.com/api/auth_user");
        HttpResponse resp = null;
        try {
            resp = requestFactory.buildGetRequest(genericUrl).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(resp.parseAsString());
        String response = "";
        JAXBContext jc = null;
        try {

            jc = JAXBContext.newInstance(GoodreadsResponse.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            StreamSource xmlSource = new StreamSource(new StringReader(resp.parseAsString()));
            JAXBElement<GoodreadsResponse> je = unmarshaller.unmarshal(xmlSource, GoodreadsResponse.class);
            response = "Welcome to Bookster Mr. "+je.getValue().getUser().getName();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    private SpeechletResponse newAskResponse(final String stringOutput, boolean isOutputSsml,
                                             final String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
}
