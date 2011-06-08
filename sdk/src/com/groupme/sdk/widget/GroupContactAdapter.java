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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.groupme.sdk.ContactEntry;
import com.groupme.sdk.R;

import java.util.ArrayList;

public class GroupContactAdapter extends BaseAdapter {
    OnContactRemovalListener mListener;

    ArrayList<ContactEntry> mContacts;
    Context mContext;

    public GroupContactAdapter(Context context) {
        mContext = context;
        mContacts = new ArrayList<ContactEntry>();
    }

    public void setOnContactRemovalListener(OnContactRemovalListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return mContacts.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public ContactEntry getItem(int position) {
        return mContacts.get(position);
    }

    public ArrayList<ContactEntry> getItems() {
        return mContacts;
    }

    public void setItems(ArrayList<ContactEntry> contacts) {
        mContacts.clear();
        mContacts.addAll(contacts);

        notifyDataSetChanged();
    }

    public void addItems(ArrayList<ContactEntry> contacts) {
        mContacts.addAll(contacts);
        notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_group_contact, null, false);

            ViewHolder holder = new ViewHolder();
            holder.contactDisplayName = (TextView) convertView.findViewById(R.id.contact_display_name);
            holder.contactTelephoneNumber = (TextView) convertView.findViewById(R.id.contact_telephone_number);
            holder.contactBadge = (QuickContactBadge) convertView.findViewById(R.id.contact_badge);

            holder.contactRemoveButton = (ImageView) convertView.findViewById(R.id.contact_remove_button);
            holder.contactRemoveButton.setOnClickListener(mClickListener);

            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.contactDisplayName.setText(mContacts.get(position).getName());
        holder.contactTelephoneNumber.setText(mContacts.get(position).getNumber());
        
        return convertView;
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onClickRemove(v);
            }
        }
    };

    static class ViewHolder {
        TextView contactDisplayName;
        TextView contactTelephoneNumber;
        QuickContactBadge contactBadge;
        ImageView contactRemoveButton;
    }

    public interface OnContactRemovalListener {
        public void onClickRemove(View view);
    }
}
