package org.example.PocketDex.Exceptions;

public class AccessTokenExpiredException extends AuthException{
    public AccessTokenExpiredException() {
        super("Access token expired or invalid, request new access token with refresh token");
    }
}
