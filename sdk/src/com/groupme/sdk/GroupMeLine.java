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

public class GroupMeLine {
    protected String mLineId;
    protected String mUserId;
    protected String mUserName;
    protected String mLineText;

    public String getLineId() {
        return mLineId;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getLineText() {
        return mLineText;
    }
    
    public static class Builder {
        private GroupMeLine mLine;

        public Builder() {
            mLine = new GroupMeLine();
        }

        public Builder setLineId(String id) {
            mLine.mLineId = id;
            return this;
        }

        public Builder setUserId(String id) {
            mLine.mUserId = id;
            return this;
        }

        public Builder setUserName(String name) {
            mLine.mUserName = name;
            return this;
        }

        public Builder setLineText(String text) {
            mLine.mLineText = text;
            return this;
        }

        public GroupMeLine create() {
            return mLine;
        }
    }
}
