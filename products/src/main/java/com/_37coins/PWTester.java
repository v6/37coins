package com._37coins;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import org.apache.geronimo.mail.util.Hex;
import org.btc4all.webfinger.WebfingerClientException;

import com.fruitcat.bitcoin.BIP38;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.DumpedPrivateKey;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.params.MainNetParams;

public class PWTester {

	public static void main(String[] args) throws WebfingerClientException, UnsupportedEncodingException, AddressFormatException, GeneralSecurityException {

		String dk = BIP38.decrypt("MultimillionaireWheelwrightImpersonator", "6PRRxuPkqeyVFuD4PXfdcLr7kiWTT9fCSzRdDLXP4Lhtb6av7ECoeGYohe");
		ECKey key = new DumpedPrivateKey(MainNetParams.get(), dk).getKey();

		System.out.println(key.toAddress(MainNetParams.get()));
	}

}
