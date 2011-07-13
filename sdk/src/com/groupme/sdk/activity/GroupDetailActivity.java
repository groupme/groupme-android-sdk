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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

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

        /* check to see if we are allowed to read contacts. */
        PackageManager pkgManager = getPackageManager();
        int access = pkgManager.checkPermission(Manifest.permission.WRITE_CONTACTS, getPackageName());

        if (access != PackageManager.PERMISSION_GRANTED) {
            Button addButton = (Button) findViewById(R.id.btn_add_address_book);
            addButton.setVisibility(View.GONE);
        }
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

    public void saveNumberInAddressBook(View view) {
        Button addButton = (Button) view;

        addButton.setEnabled(false);
        addButton.setText(R.string.button_added_to_address_book);

        AddContactTask task = new AddContactTask(addButton);
        task.execute(mGroup.getTopic(), mGroup.getGroupNumber());
    }

    public void onRequestStarted(GroupMeRequest request) {
        
    }

    public void onRequestFailed(GroupMeRequest request) {

    }

    public void onRequestCompleted(GroupMeRequest request) {
        removeDialog(DIALOG_POSTING_MESSAGE);
    }

    static class AddContactTask extends AsyncTask<String, Void, String> {
        private WeakReference<Context> mContextRef;
        private WeakReference<Button> mButtonRef;

        public AddContactTask(Button addButton) {
            mContextRef = new WeakReference<Context>(addButton.getContext());
            mButtonRef = new WeakReference<Button>(addButton);
        }

        @Override
        protected String doInBackground(String... params) {
            Context context = mContextRef.get();

            if (context == null) return null;

            Bitmap avatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boolean compressed = avatar.compress(Bitmap.CompressFormat.PNG, 100, stream);

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            int rawContactInsertIndex = ops.size();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, params[1])
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, params[0])
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                    .withValue(Photo.PHOTO, stream.toByteArray())
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                    .withValue(Note.NOTE, context.getString(R.string.contact_groupme_label))
                    .build());

            boolean success = true;

            try {
                ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

                for (ContentProviderResult result : results) {
                    if (result.uri == null) {
                        success = false;                        
                    }
                }

                if (success) {
                    return results[0].uri.toString();
                }
            } catch (OperationApplicationException e) {
                Log.e("GroupMeSDK", "Error saving group to contacts: " + e.toString());
                return null;
            } catch (RemoteException e) {
                Log.e("GroupMeSDK", "Error saving group to contacts: " + e.toString());
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String uri) {
            Button addButton = mButtonRef.get();

            if (addButton == null) return;

            if (uri == null) {
                addButton.setText(R.string.button_add_address_book);
                addButton.setEnabled(true);
            }
        }
    }
}
