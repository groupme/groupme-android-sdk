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
package com.groupme.sdk.widget;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;
import android.widget.ResourceCursorAdapter;

import android.widget.TextView;
import com.groupme.sdk.R;
import com.groupme.sdk.util.Constants;

import java.util.ArrayList;

public class ContactListAdapter extends ResourceCursorAdapter {
    private ArrayList<String> mSelectedContacts;

    public ContactListAdapter(Context context, Cursor cursor) {
        super(context, R.layout.list_item_contact, cursor, false);
        mSelectedContacts = new ArrayList<String>();
    }

    public ArrayList<String> getSelectedContacts() {
        return mSelectedContacts;
    }

    public void setSelectedContacts(ArrayList<String> contacts) {
        mSelectedContacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.contactDisplayName.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
        holder.contactTelephoneNumber.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

        holder.contactSelected.setChecked(mSelectedContacts.contains(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY))));

        Uri contact = ContentUris.withAppendedId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));
        holder.contactBadge.assignContactUri(contact);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View view = super.newView(context, cursor, parent);

        holder.contactDisplayName = (TextView) view.findViewById(R.id.contact_display_name);
        holder.contactTelephoneNumber = (TextView) view.findViewById(R.id.contact_telephone_number);
        holder.contactBadge = (QuickContactBadge) view.findViewById(R.id.contact_badge);
        holder.contactSelected = (ContactCheckBox) view.findViewById(R.id.contact_selection_check);
        holder.contactSelected.setOnClickListener(mCheckBoxListener);

        view.setTag(holder);
        return view;
    }

    View.OnClickListener mCheckBoxListener = new View.OnClickListener() {
        public void onClick(View view) {
            ContactCheckBox check = (ContactCheckBox) view;
            String key = "";
            
            if (check.isChecked()) {
                if (!mSelectedContacts.contains(key)) {
                    mSelectedContacts.add(key);
                }
            } else {
                mSelectedContacts.remove(key);
            }

            Log.d(Constants.LOG_TAG, "Contacts selected: " + mSelectedContacts.size());
        }
    };

    static class ViewHolder {
        TextView contactDisplayName;
        TextView contactTelephoneNumber;
        QuickContactBadge contactBadge;
        ContactCheckBox contactSelected;
    }
}
