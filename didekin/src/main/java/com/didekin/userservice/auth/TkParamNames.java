package com.didekin.userservice.auth;

import static org.jose4j.jwk.JsonWebKey.KEY_TYPE_PARAMETER;
import static org.jose4j.jwk.OctetSequenceJsonWebKey.KEY_VALUE_MEMBER_NAME;
import static org.jose4j.jwt.ReservedClaimNames.AUDIENCE;
import static org.jose4j.jwt.ReservedClaimNames.EXPIRATION_TIME;
import static org.jose4j.jwt.ReservedClaimNames.ISSUER;
import static org.jose4j.jwt.ReservedClaimNames.SUBJECT;
import static org.jose4j.jwx.HeaderParameterNames.ALGORITHM;
import static org.jose4j.jwx.HeaderParameterNames.ENCRYPTION_METHOD;
import static org.jose4j.jwx.HeaderParameterNames.KEY_ID;

/**
 * User: pedro@didekin
 * Date: 10/05/2018
 * Time: 10:53
 * <p>
 * CLAIM NAMES:
 * -- "iss" identifies the principal that issued the JWT: "didekin_auth"
 * -- "sub" identifies the principal that is the subject of the JWT: userName of the recipient.
 * -- "aud" identifies the recipients that the JWT is intended for. The "aud" value is a list of case-sensitive strings.
 * -- "exp" identifies the expiration time on or after which the JWT MUST NOT be accepted for processing.
 * <p>
 * HEADER NAMES:
 * -- "alg" identifies the cryptographic algorithm used to encrypt or determine the value of the content encryption key (CEK).
 * -- "enc" identifies the content encryption algorithm used to produce the ciphertext and the Authentication Tag.
 * -- "kid" is used in this implementation to hint the symmetric key needed to decrypt the JWE.
 * -- "jwk" is the public key to which the JWE was encrypted. It can be used to determine the private key needed to decrypt the JWE.
 * <p>
 * KEYS:
 * -- "kty" (key type): "oct" symmetric keys, "EC" and "RSA" for PKs.
 * -- "k" (key value) is the base64url encoding of the octet sequence containing the key value in symmetric keys.
 * -- "kid" as key parameter is not necessary. When used with JWS or JWE, the "kid" value is used to match a JWS or JWE "kid" header parameter value.
 */
public enum TkParamNames {

    issuer(ISSUER),
    subject(SUBJECT),
    audience(AUDIENCE),
    expiration(EXPIRATION_TIME),
    // HEADER
    algorithm_cek(ALGORITHM),
    algorithm_ce(ENCRYPTION_METHOD),
    keyId(KEY_ID), // NOT USED.
    jwkey("jwk"),
    // KEYS
    keyType(KEY_TYPE_PARAMETER),
    simmetricKeyValue(KEY_VALUE_MEMBER_NAME),;

    private final String name;

    TkParamNames(String nameIn)
    {
        name = nameIn;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
