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

import android.util.Log;
import com.groupme.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupMeGroup {
    protected String mTopic;
    protected String mGroupNumber;
    protected String mDescription;
    protected String mGroupId;
    
    protected GroupMeLine mLastLine;

    protected int mGroupSize;

    private GroupMeGroup() { }

    public String getGroupId() {
        return mGroupId;
    }
    
    public String getTopic() {
        return mTopic;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getGroupNumber() {
        return mGroupNumber;
    }

    public int getGroupSize() {
        return mGroupSize;
    }

    public GroupMeLine getLastLine() {
        return mLastLine;
    }

    public static GroupMeGroup fromJSON(String json) {
        GroupMeGroup group = new GroupMeGroup();

        try {
            JSONObject jsonObj = new JSONObject(json);
            group.mGroupId = jsonObj.getString("id");
            group.mTopic = jsonObj.getString("topic");
            group.mDescription = jsonObj.getString("description");
            group.mGroupNumber = jsonObj.getString("phone_number");
            group.mGroupSize = jsonObj.getInt("size");
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Error creating group from json: " + e.toString());    
        }

        return group;
    }

    public String toJSON() {
        try {
            JSONObject group = new JSONObject();
            group.put("id", mGroupId);
            group.put("topic", mTopic);
            group.put("phone_number", mGroupNumber);
            group.put("description", mDescription);
            group.put("size", mGroupSize);

            return group.toString();
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Error creating JSON representation: " + e.toString());
        }

        return null;
    }

    public static class Builder {
        private GroupMeGroup mGroup;

        public Builder() {
            mGroup = new GroupMeGroup();    
        }

        public Builder setGroupId(String id) {
            mGroup.mGroupId = id;
            return this;
        }
        
        public Builder setTopic(String topic) {
            mGroup.mTopic = topic;
            return this;
        }

        public Builder setGroupNumber(String number) {
            mGroup.mGroupNumber = number;
            return this;
        }

        public Builder setGroupSize(int size) {
            mGroup.mGroupSize = size;
            return this;
        }

        public Builder setGroupDescription(String description) {
            mGroup.mDescription = description;
            return this;
        }

        public Builder setLastLine(GroupMeLine line) {
            mGroup.mLastLine = line;
            return this;
        }

        public GroupMeGroup create() {
            return mGroup;
        }
    }
}
