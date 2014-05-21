package com._37coins;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.ldap.CryptoUtils;
import com._37coins.persistence.dao.Gateway;
import com.google.inject.Inject;

public class AuthorizingRealm extends JdbcRealm {
	
 	private final GenericRepository dao;
 	protected boolean permissionsLookupEnabled = true;
 	
	
 	@Inject
	public AuthorizingRealm(PersistenceManagerFactory pmf){
 		super();
		dao = new GenericRepository(pmf);
	}
 	
 	@Override
 	public CredentialsMatcher getCredentialsMatcher() {
 	    HashedCredentialsMatcher cm = new HashedCredentialsMatcher("SHA");
 	    cm.setStoredCredentialsHexEncoded(false);
 	    return cm;
 	}
	
 	@Override
 	protected AuthorizationInfo doGetAuthorizationInfo(
 	        PrincipalCollection principals) {
 	    // TODO Auto-generated method stub
 	    return super.doGetAuthorizationInfo(principals);
 	}
 	
 	
 	@Override
 	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {  
		  
 		UsernamePasswordToken upToken = (UsernamePasswordToken) token;  
		Gateway a = dao.queryEntity(new RNQuery().addFilter("email", upToken.getUsername()), Gateway.class);
		 
	    AuthenticationInfo info = null;
	    byte[] challenge = null;
        try {
            challenge = Base64.decodeBase64(a.getPassword().substring(6).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
	    byte[] hash = CryptoUtils.extractPasswordHash(challenge);
	    byte[] salt = CryptoUtils.extractSalt(challenge);
	    String tmp = null;
	    try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update("password".getBytes());
            byte[] hash1 = digest.digest(salt);
            tmp = ByteSource.Util.bytes(hash1).toHex();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    info = new SimpleAuthenticationInfo(a.getEmail(), hash, ByteSource.Util.bytes(salt), a.getEmail());
	    return info;
	}
 	
 	
 	@Override
 	protected Set<String> getRoleNamesForUser(Connection conn, String username)
 			throws SQLException {
 		return new HashSet<String>(Arrays.asList("gateway"));
 	}
 	
 	@Override
 	protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
 		return new SimpleAuthorizationInfo(new HashSet<String>(Arrays.asList("gateway")));
 	}
 	
 	

}
