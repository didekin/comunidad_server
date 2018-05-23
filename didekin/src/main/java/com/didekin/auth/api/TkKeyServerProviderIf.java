package com.didekin.auth.api;

import org.jose4j.lang.JoseException;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECParameterSpec;

import javax.crypto.SecretKey;

/**
 * User: pedro@didekin
 * Date: 11/05/2018
 * Time: 18:24
 */
public interface TkKeyServerProviderIf {

    // TODO: utilizar Bounty Castle BCFKS in app.

    SecretKey getCurrentKeyForTk();

    SecretKey getNewSymmetricKey() throws NoSuchAlgorithmException;

    SecretKey getNewSymmetricKey(String algorithm, int keySizeInBits) throws NoSuchAlgorithmException;

    KeyPair getNewPkiKeys(String algorithmStr, int sizeKey) throws NoSuchAlgorithmException;

    KeyPair getNewEcKeys() throws NoSuchAlgorithmException;

    KeyPair getNewEcKeys(ECParameterSpec parameterSpec) throws JoseException;
}
