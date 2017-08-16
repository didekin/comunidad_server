package com.didekin.userservice.repository;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 17:26
 */
public enum PswdGenerator {

    GENERATOR_13 {
        @Override
        public String makePswd()
        {
            return new BigInteger(65, new SecureRandom()).toString(36);
        }
    },;

    public abstract String makePswd();

//  ===============  Static members  =====================

    static final short LENGTH = 13;
}