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

import android.content.Intent;
import android.test.mock.MockContext;

public class GroupMeContext extends MockContext {
    public static final int REQUEST_DOWNLOAD_APP = 0x1;

    int mRequestType;

    public void setRequestType(int requestType) {
        mRequestType = requestType;
    }
    
    @Override
    public void startActivity(Intent intent) {
        switch (mRequestType) {
        case REQUEST_DOWNLOAD_APP:
            boolean isMarketUri = intent.getData().toString().equals("market://details?id=com.groupme.android");

            if (!isMarketUri) {
                throw new IllegalArgumentException("Uri is not pointing to the GroupMe Android application.");
            }

            break;
        }
    }
}
