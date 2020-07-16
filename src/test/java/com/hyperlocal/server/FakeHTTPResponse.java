package com.hyperlocal.server;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

public class FakeHTTPResponse implements HttpResponse<String> {

    public String response;

    public FakeHTTPResponse(String response) {
        this.response = response;
    }

    @Override
    public String body() {
        return this.response;
    }

    @Override
    public HttpHeaders headers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpRequest request() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int statusCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public URI uri() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Version version() {
        // TODO Auto-generated method stub
        return null;
    }
    
}