package com.didekin.userservice.repository;

import java.util.Random;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 17:26
 */
enum PswdGenerator {

    GENERATOR_13 {
        @Override
        public String makePswd()
        {
            final Random random = new Random();
            final char[] buf = new char[LENGTH];

            for (int i = 0; i < buf.length; ++i)
                buf[i] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }
    },;

    public abstract String makePswd();

//  ===============  Static members  =====================

    static final short LENGTH = 13;
    static final char[] symbols;

    static {

        StringBuilder tmp = new StringBuilder();

        for (char ch = '1'; ch <= '9'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'a'; ch < 'l'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'l' + 1; ch < 'o'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'o' + 1; ch <= 'z'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'A'; ch < 'O'; ++ch) {
            tmp.append(ch);
        }
        for (char ch = 'O' + 1; ch <= 'Z'; ++ch) {
            tmp.append(ch);
        }

        symbols = tmp.toString().toCharArray();
    }
}

/*import java.security.SecureRandom;

public final class SessionIdentifierGenerator {
  private SecureRandom random = new SecureRandom();

  public String nextSessionId() {
    return new BigInteger(130, random).toString(32);
  }
}*/