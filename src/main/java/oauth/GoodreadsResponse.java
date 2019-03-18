package oauth;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GoodreadsResponse {

    @XmlElement(name = "user" )
    private User user;

    @XmlElement(name = "Request" )
    private Request request;

    public User getUser() {
        return user;
    }
}
