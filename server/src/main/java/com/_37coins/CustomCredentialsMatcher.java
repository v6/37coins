package com._37coins;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;

public class CustomCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token,
            AuthenticationInfo info) {
        SimpleAuthenticationInfo sai = ((SimpleAuthenticationInfo)info);
        // Re-create the hash using the password and the extracted salt
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String pw = new String((char[])token.getCredentials());
        digest.update(pw.getBytes());
        byte[] hash = digest.digest(sai.getCredentialsSalt().getBytes());
        // See if our extracted hash matches what we just re-created
        return Arrays.equals((byte[])sai.getCredentials(), hash);
    }

}
