package oauth;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class User implements Serializable
{
	private static final long serialVersionUID = 0L;

	@XmlAttribute(name="id")
	private String idString;

	@XmlElement(name = "name" )
	private String mName;

	@XmlAttribute(name="link")
	private String linkText;
	
	public String getName() {
		return mName;
	}


	public String getId() {
		return idString;
	}

	public String getLink() {
		return linkText;
	}
}
