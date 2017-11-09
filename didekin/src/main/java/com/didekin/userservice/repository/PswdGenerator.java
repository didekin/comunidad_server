package com.didekin.userservice.repository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.asciiList;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 17:26
 */
public class PswdGenerator {

    public static final int default_password_length = 14;
    private final int pswdLength;

    PswdGenerator()
    {
        this(default_password_length);
    }

    PswdGenerator(int pswdLength)
    {
        this.pswdLength = pswdLength;
    }

    String makePassword()
    {
        byte[] bytesPswd = new byte[pswdLength];
        SecureRandom secureRnd = new SecureRandom();
        for (int i = 0; i < pswdLength; ++i) {
            bytesPswd[i] = asciiList.get(secureRnd.nextInt(asciiList.size())).byteValue();
        }
        return new String(bytesPswd);
    }

    public enum AsciiInterval {

        number(49, 57),
        letter_upper_1(65, 78),
        letter_upper_2(80, 90),
        underscore(95, 95),
        letter_lower(97, 122),;

        static final List<Integer> asciiList = new ArrayList<>(61); // aproximate guess.

        static {
            for (AsciiInterval interval : values()) {
                interval.addAsciiToList(asciiList);
            }
        }

        final int asciiInitial;
        final int asciiFinal;

        AsciiInterval(int asciiInitialInclusive, int asciiFinalInclusive)
        {
            this.asciiInitial = asciiInitialInclusive;
            this.asciiFinal = asciiFinalInclusive;
        }

        public boolean isInside(int asciiChar)
        {
            return (asciiChar >= asciiInitial && asciiChar <= asciiFinal);
        }

        public void addAsciiToList(List<Integer> symbolAsciiList)
        {
            for (int i = asciiInitial; i <= asciiFinal; ++i) {
                symbolAsciiList.add(i);
            }
        }
    }
}