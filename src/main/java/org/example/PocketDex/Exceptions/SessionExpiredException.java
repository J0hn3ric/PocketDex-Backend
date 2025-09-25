package org.example.PocketDex.Exceptions;

public class SessionExpiredException extends AuthException{
    public SessionExpiredException() {
        super("Session expired and Refresh Token expired, anonymous login to supabase required");
    }
}
