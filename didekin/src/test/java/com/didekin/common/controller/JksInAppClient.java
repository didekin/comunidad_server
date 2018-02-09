package com.didekin.common.controller;

import com.didekinlib.http.JksInClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: pedro@didekin
 * Date: 09/02/2018
 * Time: 13:00
 */
public final class JksInAppClient implements JksInClient {

    /**
     * File path.
     */
    private String jksUri;
    private String jksPswd;

    JksInAppClient(String jksUri, String jksPswd)
    {
        this.jksUri = jksUri;
        this.jksPswd = jksPswd;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(jksUri);
    }

    @Override
    public String getJksPswd()
    {
        return jksPswd;
    }

}
