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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

import com.groupme.sdk.R;
import com.groupme.sdk.util.Constants;
import com.groupme.sdk.widget.ContactListAdapter;

public class AddContactActivity extends ListActivity {
    public static final int DIALOG_ADD_NEW_CONTACT = 0x1;
    public static final int DIALOG_CONTACT_ERROR = 0x2;
    
    public static final int REQUEST_CONTACTS_SELECT = 0x1010;
    
    public static final int CONTACTS_SELECTED = 0x1111;
    public static final int CONTACTS_NOT_SELECTED = 0x22222;
    
    ContactListAdapter mAdapter;
    ArrayList<String> mManualContacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcontacts);

        /* check to see if we are allowed to read contacts. */
        PackageManager pkgManager = getPackageManager();
        int access = pkgManager.checkPermission(Manifest.permission.READ_CONTACTS, getPackageName());

        if (access == PackageManager.PERMISSION_GRANTED) {
            queryContactsProvider();
        }

        View divider = findViewById(R.id.header_divider_right);
        divider.setVisibility(View.VISIBLE);

        ImageView addGroupButton = (ImageView) findViewById(R.id.header_btn_right);
        addGroupButton.setImageResource(R.drawable.ic_menu_invite_white);
        addGroupButton.setVisibility(View.VISIBLE);

        getListView().setBackgroundColor(Color.TRANSPARENT);
        getListView().setCacheColorHint(Color.TRANSPARENT);

        mManualContacts = new ArrayList<String>();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog.Builder builder;

        switch (id) {
        case DIALOG_ADD_NEW_CONTACT:
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_new_contact_title));

            View view = getLayoutInflater().inflate(R.layout.dialog_new_contact, null, false);
            builder.setView(view);

            final EditText contactName = (EditText) view.findViewById(R.id.dialog_new_contact_name);
            final EditText contactNumber = (EditText) view.findViewById(R.id.dialog_new_contact_number);

            DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();

                        if (!TextUtils.isEmpty(contactName.getText().toString()) && !TextUtils.isEmpty(contactNumber.getText().toString())) {
                            addNewContact(contactName.getText().toString(), contactNumber.getText().toString());
                        } else {
                            showDialog(DIALOG_CONTACT_ERROR);
                        }

                        break;
                    default:
                        Log.e(Constants.LOG_TAG, "Unknown button selection: " + which);
                    }
                }
            };

            builder.setNegativeButton(R.string.button_cancel, dialogListener);
            builder.setPositiveButton(R.string.button_add, dialogListener);

            return builder.create();
        case DIALOG_CONTACT_ERROR:
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_error_contact_title));
            builder.setMessage(getString(R.string.dialog_error_contact_message));
            builder.setNeutralButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            
            return builder.create();
        default:
            throw new IllegalArgumentException("Unknown dialog id: " + id);
        }
    }

    public void queryContactsProvider() {
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String selection = ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = 1";
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, sortOrder);
        mAdapter = new ContactListAdapter(this, cursor);

        ArrayList<String> contacts = getIntent().getStringArrayListExtra("selected_contacts");

        if (contacts != null) {
            mAdapter.setSelectedContacts(contacts);
        }
        
        setListAdapter(mAdapter);
    }

    public void addNewContact(String name, String number) {
        mManualContacts.add(name + "&&" + number);
    }
    
    public void onFinishSelectingContacts(View view) {
        ArrayList<String> contacts = mAdapter.getSelectedContacts();

        if (contacts.size() != 0 || mManualContacts.size() != 0) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("selected_contacts", mAdapter.getSelectedContacts());
            intent.putStringArrayListExtra("manual_contacts", mManualContacts);
            
            setResult(CONTACTS_SELECTED, intent);
        } else {
            setResult(CONTACTS_NOT_SELECTED);
        }
        
        finish();
    }

    public void onClickNewContact(View view) {
        showDialog(DIALOG_ADD_NEW_CONTACT);
    }
}
