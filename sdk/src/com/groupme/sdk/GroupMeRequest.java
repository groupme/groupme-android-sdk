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
package com.groupme.sdk;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.groupme.sdk.net.HttpResponseException;
import com.groupme.sdk.util.Constants;
import com.groupme.sdk.util.HttpUtils;
import org.apache.http.client.HttpClient;

import java.lang.ref.WeakReference;

public class GroupMeRequest {
    public static final String CLIENT_AUTHORIZE = Constants.BASE_API_HTTPS.concat("/clients/tokens");
    public static final String CLIENT_GROUP = Constants.BASE_API_HTTPS.concat("/clients/groups");
    public static final String CLIENT_LINES = Constants.BASE_API_HTTPS.concat("/clients/groups/%s/lines");
    public static final String CLIENT_CONFERENCE = Constants.BASE_API_HTTPS.concat("/clients/groups/%s/conferences");
	public static final String CLIENT_USERS = Constants.BASE_API_HTTPS.concat("/clients/users/%s");
	
    Bundle mParams;
    String mRequest;
    String mBody;
    int mRequestMethod;

    RequestListener mListener;
    HttpClient mClient;
    String mResponse;

    public void setRequest(String request, int method) {
        mRequest = request;
        mRequestMethod = method;
    }

    public void setRequestListener(RequestListener listener) {
        mListener = listener;
    }

    public void setParams(Bundle params) {
        mParams = params;
    }

    public void start() {
        RequestTask task = new RequestTask(this);
        task.execute();

        if (mListener != null) {
            mListener.onRequestStarted(this);
        }
    }

    protected void setResponse(String response) {
        mResponse = response;
    }

    public String getResponse() {
        return mResponse;
    }

	public String getRequest() {
		return mRequest;
	}

    public void setBody(String body) {
        mBody = body;
    }
    
    protected void setHttpClient(HttpClient client) {
        mClient = client;
    }

    static class RequestTask extends AsyncTask<String, Void, String> {
        WeakReference<GroupMeRequest> mRequestRef;

        public RequestTask(GroupMeRequest request) {
            mRequestRef = new WeakReference<GroupMeRequest>(request);
        }
        
        @Override
        protected String doInBackground(String... params) {
            GroupMeRequest request = mRequestRef.get();

            if (request == null) {
                cancel(true);
                return null;
            }

            try {
                HttpClient client = request.mClient;

                if (client != null) {
                    return HttpUtils.openUrl(client, request.mRequest, request.mRequestMethod, request.mBody, request.mParams, null);
                } else {
                    return HttpUtils.openUrl(request.mRequest, request.mRequestMethod, request.mBody, request.mParams, null);
                }
            } catch (HttpResponseException e) {
                Log.e(Constants.LOG_TAG, "Response exception: " + e.toString());

                if (e.getResponseCode() == 401) {
                    return "Unauthorized";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            GroupMeRequest request = mRequestRef.get();
            
            if (request == null) {
                return;
            }

            request.setResponse(response);

            if (response == null && request.mListener != null) {
                request.mListener.onRequestFailed(request);
                return;
            }

            request.setResponse(response);

            if (request.mListener != null) {
                request.mListener.onRequestCompleted(request);
            }
        }
    }

    /**
     * Listener interface that provides callbacks on the state of the GroupMeRequest.
     *
     * @since 1
     */
    public interface RequestListener {
        /**
         * Notifies the listener the request has been started.
         *
         * @param request GroupMeRequest object that has started processing.
         * @since 1
         */
        public void onRequestStarted(GroupMeRequest request);

        /**
         * Notifies the listener the request has failed.
         *
         * @param request GroupMeRequest object that has failed.
         * @since 1
         */
        public void onRequestFailed(GroupMeRequest request);

        /**
         * Notifies the listener the request has completed.
         *
         * @param request GroupMeRequest object that has completed.
         * @since 1
         */
        public void onRequestCompleted(GroupMeRequest request);
    }
}
