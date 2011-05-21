/*
 * Copyright 2011 Mindless Dribble, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupme.sdk.mock;

import android.content.Context;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class MockHttpClient implements HttpClient {
    BasicHttpParams mParams = new BasicHttpParams();
    Context mContext;
    MockHttpResponse mResponse;
    HttpUriRequest mRequest;

    public void setContext(Context context) {
        mContext = context;
    }

    public HttpUriRequest getRequest() {
        return mRequest;
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        mRequest = request;

        URI uri = request.getURI();
        String file = uri.getPath().substring(1);

        if (file.equals("unauthorized_request")) {
            mResponse = new MockHttpResponse();
            mResponse.setStatusCode(401);
            mResponse.setReasonPhrase("Unauthorized");

            MockHttpEntity entity = new MockHttpEntity(mContext, file);
            entity.consumeContent();

            mResponse.setEntity(entity);
            return mResponse;
        }

        if (file.equals("ok_request")) {
            mResponse = new MockHttpResponse();
            mResponse.setStatusCode(200);
            mResponse.setReasonPhrase("OK");

            MockHttpEntity entity = new MockHttpEntity(mContext, file);
            entity.consumeContent();

            mResponse.setEntity(entity);
            return mResponse;
        }
        
        return null;
    }

    public HttpResponse execute(HttpUriRequest request, HttpContext context) {
        return null;
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) {
         return null;
    }

    public <T> T execute(HttpHost target, ResponseHandler<? extends T> responseHandler) {
         return null;
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
         return null;
    }

    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
         return null;
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
         return null;
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) {
         return null;
    }

    public HttpResponse execute(HttpHost target, HttpRequest request) {
        return null;
    }

    public MockHttpResponse getMockResponse() {
        return mResponse;
    }

    public ClientConnectionManager getConnectionManager() {
        Scheme http = new Scheme("http", new PlainSocketFactory(), 80);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(http);

        return new SingleClientConnManager(mParams, registry);
    }

    public HttpParams getParams() {
        return mParams;
    }
}
