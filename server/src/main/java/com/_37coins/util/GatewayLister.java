package com._37coins.util;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.ws.rs.WebApplicationException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;

import com._37coins.web.Queue;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GatewayLister {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final String ldapUrl = "ldap://localhost:1389";
        final String ldapUser = "user";
        final String ldapPw = "password";
        final String ldapBaseDn = "basedn";
        JndiLdapContextFactory jlc = new JndiLdapContextFactory();
        jlc.setUrl(ldapUrl);
        jlc.setAuthenticationMechanism("simple");
        jlc.setSystemUsername(ldapUser);
        jlc.setSystemPassword(ldapPw);
        InitialLdapContext ctx = null;
        AuthenticationToken at = new UsernamePasswordToken(ldapUser, ldapPw);
        try {
            ctx = (InitialLdapContext)jlc.getLdapContext(at.getPrincipal(),at.getCredentials());
            NamingEnumeration<?> namingEnum = null;

            ctx.setRequestControls(null);
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[]{"mail","mobile","createTimestamp","cn","departmentNumber","description","preferredLanguage","userPassword"});
            namingEnum = ctx.search("ou=gateways," + ldapBaseDn,"(objectClass=person)", searchControls);
            
            while (namingEnum.hasMore()) {
                Attributes atts = ((SearchResult) namingEnum.next())
                        .getAttributes();
                String cn = (String) atts.get("cn").get();
                //ignore system accounts
                if (cn.length()<15)
                    continue;
                String mail = (null!=atts.get("mail"))?(String) atts.get("mail").get():null;
                String mobile = (null!=atts.get("mobile"))?(String) atts.get("mobile").get():null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                boolean active = false;
                if (null==mobile){
                    //System.out.println("not migrated" + cn);
                }else{
                    NamingEnumeration<?> children = ctx.search("ou=accounts,"
                            + ldapBaseDn,
                            "(&(objectClass=person)(manager=cn="+cn+",ou=gateways,"+ldapBaseDn+"))", searchControls);
                    if (!children.hasMore()){
                        //System.out.println("not migrated" + cn);
                    }else{
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        credsProvider.setCredentials(new AuthScope(
                                "localhost", 15672),
                                new UsernamePasswordCredentials("username","password"));
                        HttpClient client = HttpClientBuilder.create()
                                .setDefaultCredentialsProvider(credsProvider).build();
                        try {
                            HttpGet someHttpGet = new HttpGet("http://localhost:15672/api/queues/%2f/" + cn);
                            URI uri = new URIBuilder(someHttpGet.getURI()).build();
                            HttpRequestBase request = new HttpGet(uri);
                            HttpResponse response = client.execute(request);
                            if (new ObjectMapper().readValue(
                                    response.getEntity().getContent(), Queue.class)
                                    .getConsumers() > 0) {
                                active = true;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("gateway: "+cn+ " with email "+ mail + " is " + ((active)?"active":"inactive"));
                    }
                    children.close();
                }
            }
        } catch (IllegalStateException | NamingException e) {
            e.printStackTrace();
            throw new WebApplicationException(e);
        }
    }

}
