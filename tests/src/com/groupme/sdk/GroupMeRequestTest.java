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

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.groupme.sdk.mock.MockHttpClient;
import com.groupme.sdk.util.HttpUtils;

public class GroupMeRequestTest extends AndroidTestCase {
    static final String CLIENT_ID = "zAhPw32tRk2WvoGG";
    static final String CLIENT_SECRET = "BGXAMIrvM9YVgZIIW2kDFFHKsQ2K9mDd";
    
    public void testProperFieldAssignment() {
        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(GroupMeRequest.CLIENT_AUTHORIZE, HttpUtils.METHOD_POST);
        request.setHttpClient(new MockHttpClient());

        Bundle params = new Bundle();
        params.putString("param_one", "one");
        params.putString("param_two", "two");

        request.setParams(params);

        assertEquals(GroupMeRequest.CLIENT_AUTHORIZE, request.mRequest);
        assertEquals(HttpUtils.METHOD_POST, request.mRequestMethod);
        assertEquals(params, request.mParams);
    }

    public void testPinRequest() {
        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(GroupMeRequest.CLIENT_AUTHORIZE, HttpUtils.METHOD_POST);
        request.setHttpClient(new MockHttpClient());

        Bundle params = new Bundle();
        params.putString("client_id", CLIENT_ID);
        params.putString("client_secret", CLIENT_SECRET);
        params.putString("device_id", "my-super-cool-device-id");
        params.putString("grant_type", "client_credentials");
        params.putString("phone_number", "(555) 555-5555");

        request.setParams(params);

        MockHttpClient client = new MockHttpClient();

    }
}
