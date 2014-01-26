package com._37coins.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.MessagingServletConfig;
import com._37coins.util.FiatPriceProvider;
import com._37coins.web.GatewayUser;
import com._37coins.web.Seller;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.MessageAddress.MsgType;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Path(ParserResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ParserResource {
	public final static String PATH = "/parser";
	
	final private List<DataSet> responseList;
	final private InitialLdapContext ctx;
	final private ObjectMapper mapper;
	final private Cache cache;
	final private FiatPriceProvider fiatPriceProvider;
	
	@SuppressWarnings("unchecked")
	@Inject public ParserResource(ServletRequest request,
			Cache cache, FiatPriceProvider fiatPriceProvider) {
		this.cache = cache;
		HttpServletRequest httpReq = (HttpServletRequest)request;
		responseList = (List<DataSet>)httpReq.getAttribute("dsl");
		DataSet ds = (DataSet)httpReq.getAttribute("create");
		if (null!=ds)
			responseList.add(ds);
		this.ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
		this.fiatPriceProvider = fiatPriceProvider;
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
	}
	
	@POST
	@Path("/Balance")
	public Response balance(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	@POST
	@Path("/Transactions")
	public Response transactions(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	@POST
	@Path("/DepositReq")
	public Response depositReq(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	@POST
	@Path("/Help")
	public Response help(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/WithdrawalReq") @SuppressWarnings("unchecked")
	public Response withdrawalReq(){
		DataSet data = responseList.get(0);
		Withdrawal w = (Withdrawal)data.getPayload();
		if (null!= w.getMsgDest() && w.getMsgDest().getAddress()!=null){
			
			String cn = null;
			String gwDn = null;
			String gwAddress = null;
			String gwLng = null;
			try{
				Attributes atts = BasicAccessAuthFilter.searchUnique("(&(objectClass=person)("+((w.getMsgDest().getAddressType()==MsgType.SMS)?"mobile":"mail")+"="+w.getMsgDest().getAddress()+"))", ctx).getAttributes();
				cn = (atts.get("cn")!=null)?(String)atts.get("cn").get():null;
				gwDn = (atts.get("manager")!=null)?(String)atts.get("manager").get():null;
			}catch(NameNotFoundException e){
				if (w.getMsgDest().getAddressType()==MsgType.SMS){//create a new user
					//set gateway from referring user's gateway
					if (data.getTo().getAddressType() == MsgType.SMS 
							&& w.getMsgDest().getPhoneNumber().getCountryCode() == data.getTo().getPhoneNumber().getCountryCode()){
						gwDn = "cn="+data.getGwCn()+",ou=gateways,"+MessagingServletConfig.ldapBaseDn;
						gwAddress = data.getTo().getGateway();
					}else{//or try to find a gateway in the database
						try{
							ctx.setRequestControls(null);
							SearchControls searchControls = new SearchControls();
							searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
							searchControls.setTimeLimit(1000);
							String cc = "+" + w.getMsgDest().getPhoneNumber().getCountryCode();
							NamingEnumeration<?> namingEnum = ctx.search("ou=gateways,"+MessagingServletConfig.ldapBaseDn, "(&(objectClass=person)(mobile="+cc+"*))", searchControls);
							Element gws = cache.get("gateways");
							if (null!=gws && !gws.isExpired()){
								Set<GatewayUser> gateways = (Set<GatewayUser>)gws.getObjectValue();
								while (namingEnum.hasMore()){
									Attributes attributes = ((SearchResult) namingEnum.next()).getAttributes();
									String gwCn = (attributes.get("cn")!=null)?(String)attributes.get("cn").get():null;
									for (GatewayUser gu: gateways){
										if (gu.getId().equals(gwCn)){
											gwAddress = (attributes.get("mobile")!=null)?(String)attributes.get("mobile").get():null;
											gwLng = (attributes.get("preferredLanguage")!=null)?(String)attributes.get("preferredLanguage").get():null;
											gwDn = "cn="+gwCn+",ou=gateways,"+MessagingServletConfig.ldapBaseDn;
											break;
										}
									}
									if (null!=gwDn) break;
								}
								namingEnum.close();
							}
							if (null==gwDn){
								responseList.clear();
								responseList.add(new DataSet().setTo(data.getTo()).setAction(Action.DST_ERROR));
								return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
							}
						}catch (NamingException | JsonProcessingException e1){
							e1.printStackTrace();
							throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
						}
					}
				}else if (w.getMsgDest().getAddressType()==MsgType.EMAIL){
					Attributes atts;
					try {
						atts = BasicAccessAuthFilter.searchUnique("(&(objectClass=person)(mail="+MessagingServletConfig.imapUser+"))", ctx).getAttributes();
						String gwCn = (atts.get("cn")!=null)?(String)atts.get("cn").get():null;
					    gwDn = "cn="+gwCn + ",ou=gateways,"+MessagingServletConfig.ldapBaseDn;
					    gwAddress = (atts.get("mail")!=null)?(String)atts.get("mail").get():null;
					} catch (IllegalStateException | NamingException e1) {
						throw new RuntimeException(e1);
					}
				}
				if (null!=gwDn){
					//create new user
					Attributes attributes=new BasicAttributes();
					Attribute objectClass=new BasicAttribute("objectClass");
					objectClass.add("inetOrgPerson");
					attributes.put(objectClass);
					Attribute sn=new BasicAttribute("sn");
					Attribute cnAtr=new BasicAttribute("cn");
					String cnString = w.getMsgDest().getAddress().replace("+", "");
					cn = cnString;
					sn.add(cnString);
					cnAtr.add(cnString);
					attributes.put(sn);
					attributes.put(cnAtr);
					attributes.put("manager", gwDn);
					attributes.put((w.getMsgDest().getAddressType()==MsgType.SMS)?"mobile":"mail", w.getMsgDest().getAddress());
					attributes.put("preferredLanguage", data.getLocaleString());
					try {
						ctx.createSubcontext("cn="+cnString+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, attributes);
						//and say hi to new user
						DataSet create = new DataSet()
							.setAction(Action.SIGNUP)
							.setTo(new MessageAddress()
								.setAddress(w.getMsgDest().getAddressObject())
								.setAddressType(w.getMsgDest().getAddressType())
								.setGateway(gwAddress))
							.setCn(cnString)
							.setLocaleString((null!=gwLng)?gwLng:data.getLocaleString())
							.setService(data.getService());
						responseList.add(create);
					} catch (NamingException e1) {
						e1.printStackTrace();
						throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
			}
			if (cn!=null){
				//set our payment destination
				if (null == w.getPayDest()){
					w.setPayDest(new PaymentAddress());
				}
				w.getPayDest()
					.setAddress(cn)
					.setAddressType(PaymentType.ACCOUNT);
				w.getMsgDest()
					.setGateway(gwAddress);
			}
		}
		//set the fee
		w.setFee(data.getGwFee());
		w.setFeeAccount(data.getGwCn());
		//check that transaction amount is > fee 
		//(otherwise tx history gets screwed up)
		if (w.getAmount().compareTo(w.getFee())<=0){
			data.setAction(Action.BELOW_FEE);
			try {
				return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
			} catch (JsonProcessingException e) {
				return null;
			}
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@POST
	@Path("/WithdrawalReqOther")
	public Response withdrawalReqOther(){
		return withdrawalReq();
	}
	
	@POST
	@Path("/WithdrawalConf")
	public Response withdrawalConf(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Buy") @SuppressWarnings("unchecked")
	public Response buy(){
		DataSet data = responseList.get(0);
		PhoneNumber pn = data.getTo().getPhoneNumber();
		if (null!=pn){
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			String mobile = phoneUtil.format(pn, PhoneNumberFormat.NATIONAL);
			Element e = cache.get("market"+pn.getCountryCode());
			List<Seller> sellers = null;
			if (null!=e){
				sellers = (List<Seller>)e.getObjectValue();
			}else{
				sellers = new ArrayList<Seller>();
			}
			boolean found = false;
			for (Seller seller: sellers){
				if (seller.getMobile().equalsIgnoreCase(mobile))
					found = true;
			}
			if (found){
				responseList.clear();
			}else{
				sellers.add(new Seller().setMobile(mobile).setPrice((float)data.getPayload()));
				cache.put(new Element("market"+pn.getCountryCode(),sellers));
			}
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Sell") @SuppressWarnings("unchecked")
	public Response sell(){
		DataSet data = responseList.get(0);
		PhoneNumber pn = data.getTo().getPhoneNumber();
		if (null!=pn){
			Element e = cache.get("market"+pn.getCountryCode());
			if (null!=e){
				List<Seller> sellers = (List<Seller>)e.getObjectValue();
				data.setPayload(sellers);
			}
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Price")
	public Response getPrice(){
		DataSet data = responseList.get(0);
		data.setPayload(fiatPriceProvider.getLocalCurValue(data.getTo().getPhoneNumber()));
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/UnknownCommand")
	public Response unknown(){
		if (responseList.size()==2){
			responseList.remove(0);
		}
		try {
			if (responseList.size()>0){
				return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
			}else{
				return null;
			}
		} catch (JsonProcessingException e) {
			return null;
		}
	}	

}
