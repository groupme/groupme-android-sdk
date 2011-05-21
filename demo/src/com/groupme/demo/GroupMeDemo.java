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
package com.groupme.demo;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ListView;
import com.groupme.sdk.GroupMeConnect;
import com.groupme.sdk.GroupMeGroup;
import com.groupme.sdk.GroupMeRequest;
import com.groupme.sdk.activity.AuthorizeActivity;
import com.groupme.sdk.activity.GroupCreateActivity;
import com.groupme.sdk.activity.GroupDetailActivity;

import java.util.List;

public class GroupMeDemo extends ListActivity implements GroupMeConnect.GroupsCallback, GroupMeRequest.RequestListener {
    GroupMeConnect mConnect;
    GroupListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mConnect = GroupMeConnect.getInstance(this);
        mConnect.setAppIdWithSecret(getString(R.string.client_id), getString(R.string.client_secret));

        View divider = findViewById(R.id.header_divider_right);
        divider.setVisibility(View.VISIBLE);

        View addGroupButton = findViewById(R.id.header_btn_right);
        addGroupButton.setVisibility(View.VISIBLE);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(GroupMeDemo.this, GroupCreateActivity.class));
            }
        });
        
        getListView().setCacheColorHint(Color.TRANSPARENT);
        getListView().setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnect.requestGroupList(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* result code will either be login successful or login failed. */
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        GroupMeGroup group = mAdapter.getItem(position);
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("group", group.toJSON());
        startActivity(intent);
        
        //mConnect.openGroupInGroupMeApp(group);
    }

    public void onCreateGroupClick(View view) {
        startActivity(new Intent(this, GroupCreateActivity.class));
    }

    public void onGroupsListDownloaded(List<GroupMeGroup> groups) {
        mAdapter = new GroupListAdapter(this, groups);
        setListAdapter(mAdapter);
    }

    public void onRequestStarted(GroupMeRequest request) {

    }

    public void onRequestFailed(GroupMeRequest request) {

    }

    public void onRequestCompleted(GroupMeRequest request) {
        Log.d("GroupMeDemo", "I posted a line!");
    }
}
