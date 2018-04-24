package com.didekin.common.jwtserver;

/**
 * User: pedro@didekin
 * Date: 23/04/2018
 * Time: 13:42
 */
public class TokenConstant {
    public static final int REFRESHTK_VALIDITY_SECONDS = 60 * 24 * 60 * 60; // 60 días.
    // Access tokens parameters.
    private static final int ACCESSTK_VALIDITY_SECONDS = 12 * 60 * 60;   // 12 horas, 43200 segundos.
}
