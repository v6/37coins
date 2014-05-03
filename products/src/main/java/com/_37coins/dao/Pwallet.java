package com._37coins.dao;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.restnucleus.dao.Model;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class Pwallet extends Model{
	private static final long serialVersionUID = 8485250942894350648L;

	@Persistent
	private String encPrivKey;
	
	@Persistent
	@Index
	private Integer identifier;

	public String getEncPrivKey() {
		return encPrivKey;
	}

	public Pwallet setEncPrivKey(String encPrivKey) {
		this.encPrivKey = encPrivKey;
		return this;
	}

	public Integer getIdentifier() {
		return identifier;
	}

	public Pwallet setIdentifier(Integer identifier) {
		this.identifier = identifier;
		return this;
	}


	public void update(Model newInstance) {
		Pwallet n = (Pwallet) newInstance;
	}
	
}
