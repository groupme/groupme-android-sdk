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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.groupme.sdk.R;
import com.groupme.sdk.ContactEntry;
import com.groupme.sdk.util.Constants;

import java.util.ArrayList;

public class MixedContactListAdapter extends BaseAdapter {
    private ArrayList<ContactEntry> mContacts;
    private ArrayList<ContactEntry> mSelectedContacts;

    private Context mContext;

    public MixedContactListAdapter(Context context) {
        mContacts = new ArrayList<ContactEntry>();
        mSelectedContacts = new ArrayList<ContactEntry>();
        
        mContext = context;
    }

    public void setContactsList(ArrayList<ContactEntry> contacts) {
        mContacts.addAll(contacts);
    }

    public ArrayList<ContactEntry> getSelectedContacts() {
        return mSelectedContacts;
    }

    public void addContact(ContactEntry entry) {
        mContacts.add(entry);
        mSelectedContacts.add(entry);
        
        notifyDataSetChanged();
    }

    public void addSelectedContacts(ArrayList<ContactEntry> contacts) {
        mSelectedContacts.addAll(contacts);

        for (ContactEntry entry : contacts) {
            if (!mContacts.contains(entry)) {
                mContacts.add(entry);
            }
        }

        notifyDataSetChanged();
    }

    public int getCount() {
        return mContacts.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        return mContacts.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_contact, null, false);

            ViewHolder holder = new ViewHolder();
            holder.contactDisplayName = (TextView) convertView.findViewById(R.id.contact_display_name);
            holder.contactTelephoneNumber = (TextView) convertView.findViewById(R.id.contact_telephone_number);
            holder.contactBadge = (QuickContactBadge) convertView.findViewById(R.id.contact_badge);
            holder.contactSelected = (ContactCheckBox) convertView.findViewById(R.id.contact_selection_check);
            holder.contactSelected.setOnClickListener(mCheckBoxListener);
            
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.contactDisplayName.setText(mContacts.get(position).getName());
        holder.contactTelephoneNumber.setText(mContacts.get(position).getNumber());
        holder.contactSelected.setChecked(mSelectedContacts.contains(mContacts.get(position)));
        holder.contactSelected.setContactId(position);
        
        return convertView;
    }

    View.OnClickListener mCheckBoxListener = new View.OnClickListener() {
        public void onClick(View view) {
            ContactCheckBox check = (ContactCheckBox) view;
            ContactEntry entry = mContacts.get(check.getContactId());
            
            if (check.isChecked()) {
                if (!mSelectedContacts.contains(entry)) {
                    mSelectedContacts.add(entry);
                }
            } else {
                mSelectedContacts.remove(entry);
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
