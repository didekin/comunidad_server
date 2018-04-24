package com.didekin.common.jwtserver;

import org.junit.Test;

import java.security.Provider;
import java.security.Security;

/**
 * User: pedro@didekin
 * Date: 23/04/2018
 * Time: 18:27
 */
public class ProviderTest {

    @Test
    public void testProviderNames(){
        Provider[] installedProvs = Security.getProviders();
        for (Provider provider : installedProvs){
            System.out.print(provider.getName());
            System.out.print(": ");
            System.out.print(provider.getInfo());
            System.out.println();
        }
    }
}
