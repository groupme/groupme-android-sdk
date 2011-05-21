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
package com.groupme.sdk.util;

import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.groupme.sdk.mock.MockHttpClient;
import com.groupme.sdk.net.HttpResponseException;

public class HttpUtilsTest extends AndroidTestCase {
    public static final String UNAUTHORIZED_REQUEST = "http://api.groupme.com/unauthorized_request";
    public static final String OK_REQUEST = "http://api.groupme.com/ok_request";

    public void testEncodeParams() {
        Bundle params = new Bundle();
        params.putString("format", "json");
        params.putString("key", "thisisatestkey");
        params.putString("foo", "bar");

        String expected = "foo=bar&key=thisisatestkey&format=json";
        String encodedParams = HttpUtils.encodeParams(params);

        assertEquals(expected, encodedParams);

        Bundle nullParams = null;
        String encodedNull = HttpUtils.encodeParams(nullParams);

        assertEquals("", encodedNull);
    }

    public void testBundleToList() {
        Bundle params = new Bundle();
        params.putString("group", "myawesomegroup");
        params.putString("platform", "android");
        params.putString("locale", "us");

        List<NameValuePair> pairs = HttpUtils.bundleToList(params);
        assertNotNull(pairs);
        assertEquals(pairs.size(), params.size());

        for (NameValuePair pair : pairs) {
            assertNotNull(pair);
            assertEquals(pair.getValue(), params.getString(pair.getName()));
        }
    }

    public void testUnauthorizedRequest() {
        MockHttpClient client = new MockHttpClient();
        client.setContext(getContext());
        String response;

        try {
            response = HttpUtils.openUrl(client, UNAUTHORIZED_REQUEST, HttpUtils.METHOD_GET, null, null, null);
        } catch (HttpResponseException e) {
            Log.e(Constants.LOG_TAG, "Received response exception: " + e.toString());
            response = e.getResponseMessage();

            assertEquals(401, e.getResponseCode());
        }

        assertEquals("fix me", response);
    }

    public void testSetHttpHeaders() {
        MockHttpClient client = new MockHttpClient();
        client.setContext(getContext());
        String response = null;

        try {
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Let me in");

            response = HttpUtils.openUrl(client, OK_REQUEST, HttpUtils.METHOD_GET, null, null, headers);
        } catch (HttpResponseException e) {
            fail("Received a response exception: " + e.toString());
        }

        Header[] headers = client.getRequest().getHeaders("Authorization");

        if (headers != null && headers.length > 0) {
            assertEquals("Let me in", headers[0].getValue());
        } else {
            fail("Headers are not properly set");
        }

        if (response == null) {
            fail("Unexpected empty response");
        }
    }

    public void testSetHttpParamsGet() {
        MockHttpClient client = new MockHttpClient();
        client.setContext(getContext());
        String response = null;

        try {
            Bundle params = new Bundle();
            params.putString("group", "mygroup");
            params.putString("format", "json");

            response = HttpUtils.openUrl(client, OK_REQUEST, HttpUtils.METHOD_GET, null, params, null);
        } catch (HttpResponseException e) {
            fail("Received a response exception: " + e.toString());
        }

        String query = client.getRequest().getURI().getQuery();
        Bundle requestParams = HttpUtils.decodeParams(query);

        if (requestParams != null && !requestParams.isEmpty()) {
            assertEquals("mygroup", requestParams.getString("group"));
            assertEquals("json", requestParams.getString("format"));
        } else {
            fail("Params are not set!");
        }

        if (response == null) {
            fail("Unexpected empty response");
        }
    }

    public void testSetHttpParamsPost() {
        MockHttpClient client = new MockHttpClient();
        client.setContext(getContext());
        String response = null;

        try {
            Bundle params = new Bundle();
            params.putString("group", "mygroup");
            params.putString("format", "json");

            response = HttpUtils.openUrl(client, OK_REQUEST, HttpUtils.METHOD_POST, null, params, null);
        } catch (HttpResponseException e) {
            fail("Received a response exception: " + e.toString());
        }

        try {
            HttpPost request = (HttpPost) client.getRequest();
            InputStream in = request.getEntity().getContent();
            BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
            StringBuilder sb = new StringBuilder();

            for (String line = r.readLine(); line != null; line = r.readLine()) {
                sb.append(line);
            }

            assertTrue(!sb.toString().equals(""));

            Bundle bodyParams = HttpUtils.decodeParams(sb.toString());
            assertEquals("mygroup", bodyParams.getString("group"));
            assertEquals("json", bodyParams.getString("format"));
        } catch (IOException e) {
            fail("Error reading post body: " + e.toString());
        }

        if (response == null) {
            fail("Unexpected empty response");
        }
    }
}
