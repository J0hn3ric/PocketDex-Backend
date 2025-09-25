package org.example.PocketDex.Exceptions;

public class RefreshTokenExpiredException extends AuthException{
    public RefreshTokenExpiredException() {
        super("Refresh token expired or invalid, make anonymous log in to supabase");
    }
}
