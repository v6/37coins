package com._37coins;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManagerFactory;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.persistence.dao.Gateway;
import com.google.inject.Inject;

public class AuthorizingRealm extends JdbcRealm {
	
 	private final GenericRepository dao;
 	protected boolean permissionsLookupEnabled = false; 
	
 	@Inject
	public AuthorizingRealm(PersistenceManagerFactory pmf){
 		super();
		dao = new GenericRepository(pmf);
	}
	
 	@Override
 	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {  
		  
 		UsernamePasswordToken upToken = (UsernamePasswordToken) token;  
		Gateway a = dao.queryEntity(new RNQuery().addFilter("email", upToken.getUsername()), Gateway.class);
		 
	    AuthenticationInfo info = null;  
	    info = new SimpleAuthenticationInfo(a.getEmail(), a.getPassword(), a.getEmail());
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
