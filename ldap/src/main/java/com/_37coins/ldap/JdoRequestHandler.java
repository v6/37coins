package com._37coins.ldap;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManagerFactory;

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.persistence.dao.Gateway;
import com.unboundid.ldap.listener.LDAPListenerClientConnection;
import com.unboundid.ldap.listener.LDAPListenerRequestHandler;
import com.unboundid.ldap.protocol.AddRequestProtocolOp;
import com.unboundid.ldap.protocol.BindRequestProtocolOp;
import com.unboundid.ldap.protocol.BindResponseProtocolOp;
import com.unboundid.ldap.protocol.CompareRequestProtocolOp;
import com.unboundid.ldap.protocol.DeleteRequestProtocolOp;
import com.unboundid.ldap.protocol.ExtendedRequestProtocolOp;
import com.unboundid.ldap.protocol.LDAPMessage;
import com.unboundid.ldap.protocol.ModifyDNRequestProtocolOp;
import com.unboundid.ldap.protocol.ModifyRequestProtocolOp;
import com.unboundid.ldap.protocol.SearchRequestProtocolOp;
import com.unboundid.ldap.protocol.SearchResultDoneProtocolOp;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResultEntry;

public class JdoRequestHandler extends LDAPListenerRequestHandler {
    private final GenericRepository dao;
    private LDAPListenerClientConnection clientConnection;
    
    public JdoRequestHandler(PersistenceManagerFactory pmf){
        this.dao = new GenericRepository(pmf);
    }

    @Override
    public LDAPMessage processSearchRequest(final int messageId,final SearchRequestProtocolOp request, final List<Control> controls) {
        String f = request.getFilter().toString();
        String dn = f.substring(f.indexOf("=")+1, f.lastIndexOf(")"));
        if (dn.contains(request.getBaseDN())){
            Attribute[] aa = {new Attribute("entryDN",dn)};
            SearchResultEntry entry = new SearchResultEntry(messageId, dn, aa);
            try {
                clientConnection.sendSearchResultEntry(messageId, entry);
            } catch (LDAPException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            return new LDAPMessage(messageId, new SearchResultDoneProtocolOp(
                    ResultCode.SUCCESS_INT_VALUE, dn, null, Collections.<String> emptyList()),
                    Collections.<Control> emptyList());
        }else{
            return new LDAPMessage(messageId, new SearchResultDoneProtocolOp(
                    ResultCode.NO_SUCH_OBJECT_INT_VALUE, dn, null, Collections.<String> emptyList()),
                    Collections.<Control> emptyList());
        }
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
        return null;
    }

    @Override
    public LDAPMessage processCompareRequest(final int messageId,final CompareRequestProtocolOp request, final List<Control> controls) {
        return null;
    }

    @Override
    public LDAPMessage processBindRequest(final int messageId,
            final BindRequestProtocolOp request, final List<Control> controls) {
        String gwDn = request.getBindDN();
        Gateway g = dao.queryEntity(new RNQuery().addFilter("cn", gwDn.substring(3, gwDn.indexOf(","))), Gateway.class, false);
        if (null!=g){
            String password = g.getPassword();
            dao.closePersistenceManager();
            try {
                if (CryptoUtils.verifySaltedPassword(request.getSimplePassword().getValue(),password)){
                    return new LDAPMessage(messageId, new BindResponseProtocolOp(
                            ResultCode.SUCCESS_INT_VALUE, null, null, null, null),
                            Collections.<Control> emptyList());                    
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
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
            LDAPListenerClientConnection clientConnection) throws LDAPException {
        this.clientConnection = clientConnection;
        return this;
    }
}
