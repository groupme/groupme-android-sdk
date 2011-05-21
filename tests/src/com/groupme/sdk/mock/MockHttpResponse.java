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

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MockHttpResponse implements HttpResponse {
    HttpEntity mEntity;
    HttpParams mParams = new BasicHttpParams();
    Locale mLocale = Locale.ENGLISH;
    ProtocolVersion mVersion;

    int mStatusCode;
    String mStatusReason;

    List<Header> mHeaders = new ArrayList<Header>();

    public MockHttpResponse() {
        mVersion = new ProtocolVersion("HTTP", 1, 1);
    }

    public HttpEntity getEntity() {
        return mEntity;
    }

    public Locale getLocale() {
        return mLocale;
    }

    public StatusLine getStatusLine() {
        return new BasicStatusLine(mVersion, mStatusCode, mStatusReason);
    }

    public void setEntity(HttpEntity entity) {
        mEntity = entity;
    }

    public void setLocale(Locale loc) {
        mLocale = loc;
    }

    public void setReasonPhrase(String reason) {
        mStatusReason = reason;
    }

    public void setStatusCode(int code) {
        mStatusCode = code;
    }

    public void setStatusLine(ProtocolVersion ver, int code) {
        mStatusCode = code;
        mVersion = ver;
    }

    public void setStatusLine(StatusLine line) {
        mVersion = line.getProtocolVersion();
        mStatusCode = line.getStatusCode();
        mStatusReason = line.getReasonPhrase();
    }

    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        mVersion = ver;
        mStatusCode = code;
        mStatusReason = reason;
    }

    public void setParams(HttpParams params) {
        mParams = params;
    }

    public HttpParams getParams() {
        return mParams;
    }

    public void addHeader(String name, String value) {
        mHeaders.add(new BasicHeader(name, value));
    }

    public void addHeader(Header header) {
        mHeaders.add(header);
    }

    public boolean containsHeader(String name) {
        return false;
    }

    public Header[] getAllHeaders() {
        return mHeaders.toArray(new Header[mHeaders.size()]);
    }

    public Header getFirstHeader(String name) {
        return null;
    }

    public Header getLastHeader(String name) {
        return null;
    }

    public Header[] getHeaders(String name) {
        return null;
    }

    public ProtocolVersion getProtocolVersion() {
        return mVersion;
    }

    public HeaderIterator headerIterator(String name) {
        return null;
    }

    public HeaderIterator headerIterator() {
        return null;
    }

    public void removeHeader(Header header) {

    }

    public void removeHeaders(String name) {

    }

    public void setHeader(Header header) {

    }

    public void setHeader(String name, String value) {

    }

    public void setHeaders(Header[] headers) {

    }
}
