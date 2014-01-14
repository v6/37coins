package com._37coins.plivo;

import java.util.Locale;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
 
@XmlRootElement(name="Speak")
public class Speak {
	
	private String text;
	
	private String language;
	
	private String voice;

	public String getLanguage() {
		return language;
	}

	@XmlAttribute
	public Speak setLanguage(String language) {
		this.language = supportedByPlivo(language);
		if (this.language.equals("ru-RU") || this.language.equals("arabic")){
			this.setVoice("MAN");
		}
		return this;
	}
	
	public String getVoice() {
		return voice;
	}

	@XmlAttribute
	public Speak setVoice(String voice) {
		this.voice = voice;
		return this;
	}

	public String getText() {
		return text;
	}

	@XmlValue
	public Speak setText(String text) {
		this.text = text;
		return this;
	}

	//taken from here: http://plivo.com/docs/xml/speak/
	public static String supportedByPlivo(String language){
		String rv = null;
		if (null==language)
			return null;
		Locale l;
		String[] lang = language.split("[-_]");
		switch(lang.length){
	        case 2: l = new Locale(lang[0], lang[1]); break;
	        case 3: l = new Locale(lang[0], lang[1], lang[2]); break;
	        default: l = new Locale(lang[0]); break;
	    }
		if (l.getLanguage().equalsIgnoreCase("ar")){
			return "arabic";
		}
		if (!l.toString().contains("_")){
			l = new Locale(l.toString(),l.toString().toUpperCase());
		}
	    if (null == rv && (l.equals(new Locale("cs", "CZ")) 
	    	|| l.equals(new Locale("da", "DK"))
	    	|| l.equals(new Locale("de", "DE"))
		    || l.equals(new Locale("el", "GR"))
		    || l.equals(new Locale("en", "AU"))
		    || l.equals(new Locale("en", "CA"))
		    || l.equals(new Locale("en", "GB"))
		    || l.equals(new Locale("en", "US"))
		    || l.equals(new Locale("es", "ES"))
		    || l.equals(new Locale("es", "US"))
		    || l.equals(new Locale("fi", "FI"))
		    || l.equals(new Locale("fr", "CA"))
		    || l.equals(new Locale("fr", "FR"))
		    || l.equals(new Locale("hu", "HU"))
		    || l.equals(new Locale("it", "IT"))
		    || l.equals(new Locale("ja", "JP"))
		    || l.equals(new Locale("nl", "NL"))
		    || l.equals(new Locale("pl", "PL"))
		    || l.equals(new Locale("pt", "BR"))
		    || l.equals(new Locale("pt", "PT"))
		    || l.equals(new Locale("ru", "RU"))
		    || l.equals(new Locale("sv", "SE"))
		    || l.equals(new Locale("zh", "CN")))){
	    	rv = l.toString();
	    }
	    if (null == rv && l.getLanguage().equalsIgnoreCase("en")){
	    	rv = new Locale("en","US").toString();
	    }
	    if (null == rv && (l.getLanguage().equals("de")
	    		||l.getLanguage().equals("fr")
	    		||l.getLanguage().equals("es")
	    		||l.getLanguage().equals("pt"))){
	    	rv = new Locale(l.getLanguage(),l.getLanguage().toUpperCase()).toString();
	    }
	    if (null == rv){
	    	rv = new Locale("en","US").toString();
	    }
	    return rv.replace('_', '-');
	}
	
}
