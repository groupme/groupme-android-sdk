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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.groupme.sdk.util.Constants;
import com.groupme.sdk.util.HttpUtils;

/**
 * Drives the registration and group related functionality of the SDK.
 * 
 * @version 1
 * @since 1
 */
public class GroupMeConnect {
    public static final int CONNECT_VERSION = 1;
    public static final String CONNECT_SDK_TYPE = "android";

    protected static final String GROUP_ME_APP_PACKAGE = "com.groupme.android";

    public static final String EXTRA_TRACKBACK_COMPONENT_NAME = "com.groupme.android.EXTRA_TRACKBACK_COMPONENT_NAME";
    public static final String EXTRA_TRACKBACK_DATA_URI = "com.groupme.android.EXTRA_TRACKBACK_DATA_URI";
    public static final String EXTRA_TRACKBACK_LABEL = "com.groupme.android.EXTRA_TRACKBACK_LABEL";
    
    protected static GroupMeConnect sInstance;

    Context mContext;
    SharedPreferences mPreferences;
    
    String mAppId;
    String mSecret;

    String mDefaultGroupName;
    boolean mHasValidSession = false;

    GroupsCallback mGroupsCallback;

    private GroupMeConnect(Context context) {
        mContext = context;

        mPreferences = mContext.getSharedPreferences("groupme.prefs", Context.MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);
        mHasValidSession = !mPreferences.getString("user_id", "").equals("");
    }

    /**
     * Allows for injecting a different Context object.
     *
     * @param context Used as the context object.
     * @since 1
     */
    protected void setContext(Context context) {
        mContext = context;
    }
    
    public static GroupMeConnect getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GroupMeConnect(context);
        }

        return sInstance;
    }

    /**
     * Sets the GroupMeConnect object to use the specified app id and secret.
     * 
     * @param appId Client app id.
     * @param secret Client app secret.
     * @since 1
     */
    public void setAppIdWithSecret(String appId, String secret) {
        mAppId = appId;
        mSecret = secret;
    }

    /**
     * Returns the default group name used by the GroupMeConnect instance.
     *
     * @return The default group name being used.
     * @since 1
     */
    public String getDefaultGroupName() {
        return mDefaultGroupName;
    }

    /**
     * Sets the default group name used by the GroupMeConnect instance.
     * 
     * @param groupName The default group name to use.
     * @since 1
     */
    public void setDefaultGroupName(String groupName) {
        mDefaultGroupName = groupName;
    }

    public void authorizePhoneNumber(String phoneNumber, GroupMeRequest.RequestListener listener) {
        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(GroupMeRequest.CLIENT_AUTHORIZE, HttpUtils.METHOD_POST);
        request.setRequestListener(listener);
        
        Bundle params = new Bundle();
        params.putString("client_id", mAppId);
        params.putString("client_secret", mSecret);
        params.putString("device_id", "my-super-cool-device-id");
        params.putString("grant_type", "client_credentials");
        params.putString("phone_number", phoneNumber);

        request.setParams(params);
        request.start();
    }

    public void validatePinNumber(String phoneNumber, String pinNumber, GroupMeRequest.RequestListener listener) {
        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(GroupMeRequest.CLIENT_AUTHORIZE, HttpUtils.METHOD_POST);
        request.setRequestListener(listener);

        Bundle params = new Bundle();
        params.putString("client_id", mAppId);
        params.putString("client_secret", mSecret);
        params.putString("device_id", "my-super-cool-device-id");
        params.putString("grant_type", "authorization_code");
        params.putString("phone_number", phoneNumber);
        params.putString("code", pinNumber.toUpperCase());

        request.setParams(params);
        request.start();
    }

    public void createGroup(String groupName, ArrayList<ContactEntry> members, GroupMeRequest.RequestListener listener) {
        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(GroupMeRequest.CLIENT_GROUP, HttpUtils.METHOD_POST);
        request.setRequestListener(listener);

        Log.d(Constants.LOG_TAG, "Client id: " + mAppId);
        
        Bundle params = new Bundle();
        params.putString("client_id", mAppId);
        params.putString("token", mPreferences.getString("token", ""));

        request.setParams(params);
        
        try {
            JSONObject group = new JSONObject();
            group.put("topic", groupName);

            JSONArray groupMembers = new JSONArray();

            for (ContactEntry member : members) {
                JSONObject groupMember = new JSONObject();
                groupMember.put("name", member.getName());
                groupMember.put("phone_number", member.getNumber());

                groupMembers.put(groupMember);
            }

            group.put("memberships", groupMembers);
            request.setBody(new JSONObject().put("group", group).toString());

            Log.d(Constants.LOG_TAG, "Request: " + new JSONObject().put("group", group).toString());
        } catch (JSONException e) {
            if (listener != null) {
                listener.onRequestFailed(request);
            }

            return;
        }

        request.start();
    }

    public void requestGroupList(GroupsCallback callback) {
        if (!hasValidSession()) {
            return;
        }
        
        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(GroupMeRequest.CLIENT_GROUP, HttpUtils.METHOD_GET);
        request.setRequestListener(mGroupRequestListener);
        
        Bundle params = new Bundle();
        params.putString("client_id", mAppId);
        params.putString("token", mPreferences.getString("token", ""));

        mGroupsCallback = callback;
        request.setParams(params);
        request.start();
    }

    public void postLineToGroup(GroupMeGroup group, String line, GroupMeRequest.RequestListener listener) {
        String url = String.format(GroupMeRequest.CLIENT_LINES, group.getGroupId());

        GroupMeRequest request = new GroupMeRequest();
        request.setRequest(url, HttpUtils.METHOD_POST);
        request.setRequestListener(listener);
        
        Bundle params = new Bundle();
        params.putString("client_id", mAppId);
        params.putString("token", mPreferences.getString("token", ""));

        try {
            JSONObject lineJson = new JSONObject();
            lineJson.put("text", line);
            lineJson.put("source_guid", UUID.randomUUID());
            
            request.setBody(new JSONObject().put("line", lineJson).toString());
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Error creating line body for post: " + e.toString());

            if (listener != null) {
                listener.onRequestFailed(request);
            }

            return;
        }

        request.setParams(params);
        request.start();
    }

    public void openGroupInGroupMeApp(GroupMeGroup group) {
        if (hasGroupMeAppInstalled()) {
            Intent intent = new Intent();
            intent.setData(Uri.parse("groupme://group/id/" + group.getGroupId()));

            mContext.startActivity(intent);
        }
    }

	public void openGroupInGroupMeApp(String groupId) {
		if (hasGroupMeAppInstalled()) {
			Intent intent = new Intent();
			intent.setData(Uri.parse("groupme://group/id/" + groupId));
			
			mContext.startActivity(intent);
		}
	}
	
	public void conferenceCallGroup(GroupMeGroup group, GroupMeRequest.RequestListener listener) {
		String url = String.format(GroupMeRequest.CLIENT_CONFERENCE, group.getGroupId());
		
		GroupMeRequest request = new GroupMeRequest();
		request.setRequest(url, HttpUtils.METHOD_POST);
		request.setRequestListener(listener);
		
		Bundle params = new Bundle();
		params.putString("client_id", mAppId);
		params.putString("token", mPreferences.getString("token", ""));
		
		request.setParams(params);
		request.start();
	}
	
	public void setMemberName(String userId, String name, GroupMeRequest.RequestListener listener) {
		String url = String.format(GroupMeRequest.CLIENT_USERS, userId);
		
		GroupMeRequest request = new GroupMeRequest();
		request.setRequest(url, HttpUtils.METHOD_PUT);
		request.setRequestListener(listener);
		
		Bundle params = new Bundle();
		params.putString("client_id", mAppId);
		params.putString("token", mPreferences.getString("token", ""));
		params.putString("user[name]", name);
		
		request.setParams(params);
		request.start();
	}
	
    public boolean hasValidSession() {
        return mHasValidSession;
    }

    public void clearSession() {
        mPreferences.edit().remove("user_id").remove("token").commit();
    }
    
    /**
     * Checks with the PackageManager on device to see if the GroupMe app is installed.
     *
     * @return True if the GroupMe app is installed or false if it is not.
     * @since 1
     */
    public boolean hasGroupMeAppInstalled() {
        PackageManager manager = mContext.getPackageManager();

        try {
            PackageInfo info = manager.getPackageInfo(GROUP_ME_APP_PACKAGE, 0);
            return info.packageName.equals(GROUP_ME_APP_PACKAGE);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Opens the Android Market to download the GroupMe application.
     *
     * @since 1
     */
    public void downloadGroupMeApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("market://details?id=com.groupme.android"));
        
        mContext.startActivity(intent);
    }

    protected SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            if (key.equals("user_id")) {
                mHasValidSession = !preferences.getString(key, "").equals("");
            }
        }
    };

    static class GroupsSyncTask extends AsyncTask<String, Void, List<GroupMeGroup>> {
        WeakReference<GroupsCallback> mCallbackRef;

        public GroupsSyncTask(GroupsCallback callback) {
            mCallbackRef = new WeakReference<GroupsCallback>(callback);    
        }

        @Override
        protected List<GroupMeGroup> doInBackground(String... params) {
            String groups = params[0];

            if (groups == null) {
                cancel(true);
                return null;
            }

            List<GroupMeGroup> groupList = new ArrayList<GroupMeGroup>();
            
            try {
                JSONObject groupsObject = new JSONObject(groups).getJSONObject("response");
                JSONArray groupsArray = groupsObject.getJSONArray("groups");
                int size = groupsArray.length();

                for (int i = 0; i < size; i++) {
                    GroupMeGroup.Builder builder = new GroupMeGroup.Builder();
                    JSONObject group = groupsArray.getJSONObject(i);

                    builder.setGroupId(group.getString("id"));
                    builder.setTopic(group.getString("topic"));
                    builder.setGroupDescription(group.getString("description"));
                    builder.setGroupSize(group.getInt("size"));
                    builder.setGroupNumber(group.getString("phone_number"));

                    GroupMeLine.Builder lineBuilder = new GroupMeLine.Builder();

                    if (!group.isNull("last_line")) {
                        JSONObject line = group.getJSONObject("last_line");

                        lineBuilder.setLineId(line.getString("id"));
                        lineBuilder.setLineText(line.getString("text"));
                        lineBuilder.setUserId(line.getString("user_id"));
                        lineBuilder.setUserName(line.getString("name"));

                        GroupMeLine lastLine = lineBuilder.create();
                        builder.setLastLine(lastLine);
                    }

                    groupList.add(builder.create());
                }
            } catch (JSONException e) {
                Log.e(Constants.LOG_TAG, "JSON parse error: " + e.toString());
                
                cancel(true);
                return null;
            }
            
            return groupList;
        }

        @Override
        protected void onPostExecute(List<GroupMeGroup> groups) {
            GroupsCallback callback = mCallbackRef.get();

            if (callback != null) {
                callback.onGroupsListDownloaded(groups);
            }
        }
    }

    GroupMeRequest.RequestListener mGroupRequestListener = new GroupMeRequest.RequestListener() {
        public void onRequestStarted(GroupMeRequest request) {
            
        }

        public void onRequestFailed(GroupMeRequest request) {

        }

        public void onRequestCompleted(GroupMeRequest request) {
            if (request.getResponse().equals("Unauthorized")) {
                onRequestFailed(request);
                return;
            }

            GroupsSyncTask sync = new GroupsSyncTask(mGroupsCallback);
            sync.execute(request.mResponse);
        }
    };

    public interface GroupsCallback {
        public void onGroupsListDownloaded(List<GroupMeGroup> groups);
    }
}
