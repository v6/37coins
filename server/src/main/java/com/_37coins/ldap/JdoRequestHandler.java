package com._37coins.ldap;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.persistence.dao.Gateway;
import com.unboundid.ldap.listener.LDAPListenerClientConnection;
import com.unboundid.ldap.listener.LDAPListenerRequestHandler;
import com.unboundid.ldap.protocol.AddRequestProtocolOp;
import com.unboundid.ldap.protocol.BindRequestProtocolOp;
import com.unboundid.ldap.protocol.BindResponseProtocolOp;
import com.unboundid.ldap.protocol.CompareRequestProtocolOp;
import com.unboundid.ldap.protocol.CompareResponseProtocolOp;
import com.unboundid.ldap.protocol.DeleteRequestProtocolOp;
import com.unboundid.ldap.protocol.DeleteResponseProtocolOp;
import com.unboundid.ldap.protocol.ExtendedRequestProtocolOp;
import com.unboundid.ldap.protocol.LDAPMessage;
import com.unboundid.ldap.protocol.ModifyDNRequestProtocolOp;
import com.unboundid.ldap.protocol.ModifyRequestProtocolOp;
import com.unboundid.ldap.protocol.SearchRequestProtocolOp;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

public class JdoRequestHandler extends LDAPListenerRequestHandler {
    private final GenericRepository dao;
    
    public JdoRequestHandler(GenericRepository dao){
        this.dao = dao;
    }

    @Override
    public LDAPMessage processSearchRequest(final int messageId,final SearchRequestProtocolOp request, final List<Control> controls) {
        return null;
    }

    @Override
    public LDAPMessage processModifyRequest(final int messageId,final ModifyRequestProtocolOp request, final List<Control> controls) {
        return null;
    }

    @Override
    public LDAPMessage processModifyDNRequest(final int messageId,final ModifyDNRequestProtocolOp request, final List<Control> controls) {
        return null;
    }

    @Override
    public LDAPMessage processExtendedRequest(final int messageId,final ExtendedRequestProtocolOp request, final List<Control> controls) {
        return null;
    }

    @Override
    public LDAPMessage processDeleteRequest(final int messageId,final DeleteRequestProtocolOp request, final List<Control> controls) {
        return new LDAPMessage(messageId, new DeleteResponseProtocolOp(ResultCode.LOCAL_ERROR_INT_VALUE,null,null,null),Collections.<Control> emptyList());
    }

    @Override
    public LDAPMessage processCompareRequest(final int messageId,final CompareRequestProtocolOp request, final List<Control> controls) {
        return new LDAPMessage(messageId, new CompareResponseProtocolOp(ResultCode.LOCAL_ERROR_INT_VALUE,null,null,null),Collections.<Control> emptyList());
    }

    @Override
    public LDAPMessage processBindRequest(final int messageId,
            final BindRequestProtocolOp request, final List<Control> controls) {
        String gwDn = request.getBindDN();
        Gateway g = dao.queryEntity(new RNQuery().addFilter("cn", gwDn.substring(3, gwDn.indexOf(","))), Gateway.class, false);
        if (null!=g){
            try {
                String pw = CryptoUtils.getSaltedPassword(request.getSimplePassword().getValue(), g.getSalt());
                if (g.getPassword().equals(pw)){
                    return new LDAPMessage(messageId, new BindResponseProtocolOp(
                            ResultCode.SUCCESS_INT_VALUE, null, null, null, null),
                            Collections.<Control> emptyList());                    
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return new LDAPMessage(messageId, new BindResponseProtocolOp(
                ResultCode.INVALID_CREDENTIALS_INT_VALUE, null, null, null,
                null), Collections.<Control> emptyList());
    }

    @Override
    public LDAPMessage processAddRequest(int arg0, AddRequestProtocolOp arg1,
            List<Control> arg2) {
        return null;
    }

    @Override
    public LDAPListenerRequestHandler newInstance(
            LDAPListenerClientConnection arg0) throws LDAPException {
        return this;
    }
}
