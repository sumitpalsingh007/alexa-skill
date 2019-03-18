package oauth;

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
import org.apache.http.HttpException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * Author: davecahill
 *
 * Adapted from user Sqeezer's StackOverflow post at
 * http://stackoverflow.com/questions/15194182/examples-for-oauth1-using-google-api-java-oauth
 * to work with Goodreads' oAuth API.
 *
 * Get a key / secret by registering at https://www.goodreads.com/api/keys
 * and replace YOUR_KEY_HERE / YOUR_SECRET_HERE in the code below.
 */
public class GoodreadsOAuthSample implements HttpRequestHandler {

    public static final String BASE_GOODREADS_URL = "https://www.goodreads.com";
    public static final String TOKEN_SERVER_URL = BASE_GOODREADS_URL + "/oauth/request_token";
    public static final String AUTHENTICATE_URL = BASE_GOODREADS_URL + "/oauth/authorize";
    public static final String ACCESS_TOKEN_URL = BASE_GOODREADS_URL + "/oauth/access_token";

    public static final String GOODREADS_KEY = "D9XU6XRd6HKe0JWU5YrIaw";
    public static final String GOODREADS_SECRET = "O3vhWhA6Rs5U5zl77C6lwznKPWuSii8L95Iya3NhuQ";

    public static void main(String[] args) throws IOException, InterruptedException {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        // Get Temporary Token
        OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(TOKEN_SERVER_URL);
        signer.clientSharedSecret = GOODREADS_SECRET;
        getTemporaryToken.signer = signer;
        getTemporaryToken.consumerKey = GOODREADS_KEY;
        getTemporaryToken.transport = new NetHttpTransport();
        OAuthCredentialsResponse temporaryTokenResponse = getTemporaryToken.execute();

        // Build Authenticate URL
        OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(AUTHENTICATE_URL);
        accessTempToken.temporaryToken = temporaryTokenResponse.token;
        String authUrl = accessTempToken.build();

        // Redirect to Authenticate URL in order to get Verifier Code
        System.out.println("Goodreads oAuth sample: Please visit the following URL to authorize:");
        System.out.println(authUrl);
        System.out.println("Waiting 10s to allow time for visiting auth URL and authorizing...");
        Thread.sleep(10000);

        System.out.println("Waiting time complete - assuming access granted and attempting to get access token");
        // Get Access Token using Temporary token and Verifier Code
        OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
        accessToken.signer = signer;
        // NOTE: This is the main difference from the StackOverflow example
        signer.tokenSharedSecret = temporaryTokenResponse.tokenSecret;
        accessToken.temporaryToken = temporaryTokenResponse.token;
        accessToken.transport = new NetHttpTransport();
        accessToken.consumerKey = GOODREADS_KEY;
        OAuthCredentialsResponse accessTokenResponse = accessToken.execute();

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
        HttpResponse resp = requestFactory.buildGetRequest(genericUrl).execute();
        //System.out.println(resp.parseAsString());

        JAXBContext jc = null;
        try {

            jc = JAXBContext.newInstance(GoodreadsResponse.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            StreamSource xmlSource = new StreamSource(new StringReader(resp.parseAsString()));
            JAXBElement<GoodreadsResponse> je = unmarshaller.unmarshal(xmlSource, GoodreadsResponse.class);
            System.out.println("Hello Mr. "+je.getValue().getUser().getName());
        } catch (JAXBException e) {
            e.printStackTrace();
        }


    }

    public void handle(org.apache.http.HttpRequest httpRequest, org.apache.http.HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        // Get Temporary Token
        OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(TOKEN_SERVER_URL);
        signer.clientSharedSecret = GOODREADS_SECRET;
        getTemporaryToken.signer = signer;
        getTemporaryToken.consumerKey = GOODREADS_KEY;
        getTemporaryToken.transport = new NetHttpTransport();
        OAuthCredentialsResponse temporaryTokenResponse = getTemporaryToken.execute();

        // Build Authenticate URL
        OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(AUTHENTICATE_URL);
        accessTempToken.temporaryToken = temporaryTokenResponse.token;
        String authUrl = accessTempToken.build();

        // Redirect to Authenticate URL in order to get Verifier Code
        System.out.println("Goodreads oAuth sample: Please visit the following URL to authorize:");
        System.out.println(authUrl);
        System.out.println("Waiting 10s to allow time for visiting auth URL and authorizing...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting time complete - assuming access granted and attempting to get access token");
        // Get Access Token using Temporary token and Verifier Code
        OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
        getAccessToken.signer = signer;
        // NOTE: This is the main difference from the StackOverflow example
        signer.tokenSharedSecret = temporaryTokenResponse.tokenSecret;
        getAccessToken.temporaryToken = temporaryTokenResponse.token;
        getAccessToken.transport = new NetHttpTransport();
        getAccessToken.consumerKey = GOODREADS_KEY;
        OAuthCredentialsResponse accessTokenResponse = getAccessToken.execute();

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
        HttpResponse resp = requestFactory.buildGetRequest(genericUrl).execute();
        //System.out.println(resp.parseAsString());
        /*DOMParser parser = new DOMParser();
        try {
            parser.parse(new InputSource(new java.io.StringReader(resp.parseAsString())));
            Document doc = parser.getDocument();
            System.out.println(doc);
            System.out.println(doc.getDocumentElement());
            String message = doc.getDocumentElement().getTextContent();
            System.out.println(message);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        JAXBContext jc = null;
        try {

            jc = JAXBContext.newInstance(GoodreadsResponse.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            StreamSource xmlSource = new StreamSource(new StringReader(resp.parseAsString()));
            JAXBElement<GoodreadsResponse> je = unmarshaller.unmarshal(xmlSource, GoodreadsResponse.class);
            System.out.println("Hello " + je.getValue().getUser().getName());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
