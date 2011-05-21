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

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.ArrayList;

import android.widget.EditText;
import com.groupme.sdk.GroupMeConnect;
import com.groupme.sdk.GroupMeRequest;
import com.groupme.sdk.R;
import com.groupme.sdk.util.Constants;
import com.groupme.sdk.widget.GroupContactAdapter;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupCreateActivity extends ListActivity implements GroupContactAdapter.OnContactRemovalListener, GroupMeRequest.RequestListener {
	public static final int GROUP_CREATED = 0x181;
	public static final int CREATE_GROUP = 0x182;
	public static final int GROUP_NOT_CREATED = 0x183;
	
	public static final int DIALOG_CREATING = 0x1;
	
    ArrayList<String> mContacts;
    GroupContactAdapter mAdapter;

    Button mCreateButton;
    EditText mGroupNameEntry;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupcreate);

        GroupMeConnect connect = GroupMeConnect.getInstance(this);

        if (!connect.hasValidSession()) {
            /* fire off the authentication activity. */
            startActivityForResult(new Intent(this, AuthorizeActivity.class), AuthorizeActivity.REQUEST_LOGIN);
        }

        mCreateButton = (Button) findViewById(R.id.btn_create_group_alt);
        mCreateButton.setEnabled(false);

        mGroupNameEntry = (EditText) findViewById(R.id.group_name_entry);
        mGroupNameEntry.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence str, int start, int before, int count) {
                if (!TextUtils.isEmpty(mGroupNameEntry.getText().toString())) {
                    if (mContacts != null && mContacts.size() != 0) {
                        mCreateButton.setEnabled(true);
                    } else {
                        mCreateButton.setEnabled(false);
                    }
                } else {
                    mCreateButton.setEnabled(false);
                }
            }

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence str, int start, int before, int count) {
                
            }
        });

        getListView().setBackgroundColor(Color.TRANSPARENT);
        getListView().setCacheColorHint(Color.TRANSPARENT);
    }

	@Override
	    protected Dialog onCreateDialog(int id, Bundle args) {
	        switch (id) {
	        case DIALOG_CREATING:
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	            dialog.setMessage(getString(R.string.dialog_creating_group));

	            return dialog;
	        default:
	            throw new IllegalArgumentException("Unknown dialog id: " + id);
	        }
	    }
	
	@Override
	public void onBackPressed() {
		setResult(GROUP_NOT_CREATED);
		super.onBackPressed();
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthorizeActivity.REQUEST_LOGIN && (resultCode == AuthorizeActivity.LOGIN_FAILED || resultCode == 0)) {
            finish();
        }

        if (requestCode == AddContactActivity.REQUEST_CONTACTS_SELECT && resultCode == AddContactActivity.CONTACTS_SELECTED) {
            mContacts = data.getStringArrayListExtra("selected_contacts");
            Log.d(Constants.LOG_TAG, "Contacts selected: " + mContacts.size());

            setListData();
        }
    }

    public void setListData() {
        String[] projection = new String[] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        };

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String contact : mContacts) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append("'").append(contact).append("'");
        }

        String selection = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " IN (" + sb.toString() + ")";
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC LIMIT 1";

        Cursor cursor = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, sortOrder);
        Log.d(Constants.LOG_TAG, "Cursor count: " + cursor.getCount());

        mAdapter = new GroupContactAdapter(this, cursor);
        mAdapter.setOnContactRemovalListener(this);
        setListAdapter(mAdapter);

        if (mContacts != null && mContacts.size() != 0 && !TextUtils.isEmpty(mGroupNameEntry.getText().toString())) {
            mCreateButton.setEnabled(true);
        } else {
            mCreateButton.setEnabled(false);
        }
    }

    public void onClickRemove(View view) {
        if (mContacts == null) {
            return;
        }

        String key = (String) view.getTag();
        mContacts.remove(key);

        setListData();
    }

    public void onLaunchContactSelector(View view) {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.putStringArrayListExtra("selected_contacts", mContacts);
        startActivityForResult(intent, AddContactActivity.REQUEST_CONTACTS_SELECT);
    }

    public void onCreateGroup(View view) {
		showDialog(DIALOG_CREATING);
		
        GroupMeConnect connect = GroupMeConnect.getInstance(this);
        connect.createGroup(mGroupNameEntry.getText().toString(), mContacts, this);
    }

    /* GroupMeRequest Listener methods. */

    public void onRequestStarted(GroupMeRequest request) {

    }

    public void onRequestFailed(GroupMeRequest request) {

    }

    public void onRequestCompleted(GroupMeRequest request) {
        try {
            JSONObject response = new JSONObject(request.getResponse());
            int code = response.getJSONObject("meta").getInt("code");
			
			Log.d(Constants.LOG_TAG, "Group create response: " + response.toString());

            if (code == 201) {
				String id = response.getJSONObject("response").getJSONObject("group").getString("id");
	
				Intent intent = new Intent();
				intent.putExtra("group_id", id);
				
				removeDialog(DIALOG_CREATING);
				setResult(GROUP_CREATED, intent);
                finish();
            }
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Error parsing JSON response: " + e.toString());
        }
    }
}
