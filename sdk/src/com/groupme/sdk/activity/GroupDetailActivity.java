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
package com.groupme.sdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import com.groupme.sdk.GroupMeConnect;
import com.groupme.sdk.GroupMeGroup;
import com.groupme.sdk.GroupMeRequest;
import com.groupme.sdk.R;

public class GroupDetailActivity extends Activity implements GroupMeRequest.RequestListener {
    public static final int DIALOG_COMPOSE_MESSAGE = 0x1;
    public static final int DIALOG_POSTING_MESSAGE = 0x2;

    public static final String EXTRA_DEFAULT_MESSAGE = "com.groupme.sdk.extra.DEFAULT_MESSAGE";
    
    GroupMeConnect mConnect;
    GroupMeGroup mGroup;

    String mDefaultMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupdetail);

        mConnect = GroupMeConnect.getInstance(this);

        TextView groupNameView = (TextView) findViewById(R.id.group_name_view);

        String groupJson = getIntent().getStringExtra("group");

        if (groupJson != null) {
            mGroup = GroupMeGroup.fromJSON(groupJson);
            groupNameView.setText(mGroup.getTopic());
        }

        mDefaultMessage = getIntent().getStringExtra(EXTRA_DEFAULT_MESSAGE);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog.Builder builder;

        switch (id) {
        case DIALOG_COMPOSE_MESSAGE:
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title_post_message);

            View view = getLayoutInflater().inflate(R.layout.dialog_post, null, false);

            if (mDefaultMessage != null) {
                EditText composeView = (EditText) view.findViewById(R.id.dialog_post_entry);
                composeView.setText(mDefaultMessage);
            }

            builder.setView(view);

            builder.setPositiveButton(R.string.button_post_message, mDialogListener);
            builder.setNegativeButton(R.string.button_cancel, mDialogListener);
            
            return builder.create();
        case DIALOG_POSTING_MESSAGE:
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setMessage(getString(R.string.dialog_posting_message));
            return dialog;
        default:
            throw new IllegalArgumentException("Unknown dialog id: " + id);
        }
    }

    DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                dialog.dismiss();

                Dialog d = (Dialog) dialog;
                EditText postEntry = (EditText) d.findViewById(R.id.dialog_post_entry);
                String message = postEntry.getText().toString();

                showDialog(DIALOG_POSTING_MESSAGE);
                mConnect.postLineToGroup(mGroup, message, GroupDetailActivity.this);
                postEntry.setText("");
                
                break;
            }
        }
    };

    public void openGroupInGroupMe(View view) {
        if (mGroup != null) {
            mConnect.openGroupInGroupMeApp(mGroup);
        }
    }

    public void sendGroupTextMessage(View view) {
        showDialog(DIALOG_COMPOSE_MESSAGE);
    }

    public void onRequestStarted(GroupMeRequest request) {
        
    }

    public void onRequestFailed(GroupMeRequest request) {

    }

    public void onRequestCompleted(GroupMeRequest request) {
        removeDialog(DIALOG_POSTING_MESSAGE);
    }
}
