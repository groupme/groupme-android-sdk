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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.groupme.sdk.net.HttpResponseException;

public class HttpUtils {
    public static final int METHOD_GET = 0x1;
    public static final int METHOD_POST = 0x2;
    public static final int METHOD_PUT = 0x3;

    public static String openUrl(String url, int method, String body, Bundle params, Bundle headers) throws HttpResponseException {
        return openUrl(new DefaultHttpClient(), url, method, body, params, headers);
    }

    public static String openUrl(HttpClient client, String url, int method, String body, Bundle params, Bundle headers) throws HttpResponseException {
        String response = null;
        InputStream in = openStream(client, url, method, body, params, headers);

        try {
            if (in == null) {
                return null;
            }

            response = read(in);
            Log.d(Constants.LOG_TAG, "Response: " + response);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Error reading response: " + e.toString());
        }

        return response;
    }

    public static InputStream openStream(HttpClient client, String url, int method, String body, Bundle params, Bundle headers) throws HttpResponseException {
        HttpResponse response;
        InputStream in = null;
        
        Log.v("HttpUtils", "URL = " + url);

        try {
            switch (method) {
            case METHOD_GET:
                url = url + "?" + encodeParams(params);
                HttpGet get = new HttpGet(url);

                if (headers != null && !headers.isEmpty()) {
                    for (String header : headers.keySet()) {
                        get.setHeader(header, headers.getString(header));
                    }
                }
                
                response = client.execute(get);
                break;
            case METHOD_POST:
                if (body != null) {
                    url = url + "?" + encodeParams(params);
                }
                
                HttpPost post = new HttpPost(url);
                Log.d(Constants.LOG_TAG, "URL: " + url);
                
                if (headers != null && !headers.isEmpty()) {
                    for (String header : headers.keySet()) {
                        post.setHeader(header, headers.getString(header));
                    }
                }

                if (body == null) {
                    List<NameValuePair> pairs = bundleToList(params);
                    post.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
                } else {
                    post.setEntity(new StringEntity(body));
                }
                
                response = client.execute(post);
                break;
			case METHOD_PUT:
				if (body != null) {
					url = url + "?" + encodeParams(params);
				}
				
				HttpPut put = new HttpPut(url);
				
				if (headers != null && !headers.isEmpty()) {
					for (String header : headers.keySet()) {
						put.setHeader(header, headers.getString(header));
					}
				}
				
				if (body == null) {
					List<NameValuePair> pairs = bundleToList(params);
					put.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
				} else {
					put.setEntity(new StringEntity(body));
				}
				
				response = client.execute(put);
				break;
            default:
                throw new UnsupportedOperationException("Cannot execute HTTP method: " + method);
            }

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode > 400) {
                throw new HttpResponseException(statusCode,  read(response.getEntity().getContent()));
            }

            in = response.getEntity().getContent();
        } catch (ClientProtocolException e) {
            Log.e(Constants.LOG_TAG, "Client error: " + e.toString());
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "IO error: " + e.toString());
        }

        return in;
    }

    public static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);

        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }

        in.close();
        return sb.toString();
    }

    public static String encodeParams(Bundle params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }

            try {
                sb.append(URLEncoder.encode(key, Constants.UTF_8)).append("=").append(URLEncoder.encode(params.getString(key), Constants.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Log.e(Constants.LOG_TAG, "Unsupported encoding: " + e.toString());
            }
        }

        return sb.toString();
    }

    public static Bundle decodeParams(String query) {
        Bundle params = new Bundle();

        if (query != null && !query.equals("")) {
            String[] parts = query.split("&");

            for (String param : parts) {
                String[] item = param.split("=");

                try {
                    params.putString(URLDecoder.decode(item[0], Constants.UTF_8), URLDecoder.decode(item[1], Constants.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    Log.e(Constants.LOG_TAG, "Unsupported encoding: " + e.toString());
                }
            }
        }

        return params;
    }

    public static List<NameValuePair> bundleToList(Bundle params) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        /* TODO: URL encode the params. */
        for (String key : params.keySet()) {
            pairs.add(new BasicNameValuePair(key, params.getString(key)));
        }

        return pairs;
    }

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }
}
