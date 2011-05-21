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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.groupme.sdk.R;

public class GroupContactAdapter extends ResourceCursorAdapter {
    OnContactRemovalListener mListener;
    
    public GroupContactAdapter(Context context, Cursor cursor) {
        super(context, R.layout.list_item_group_contact, cursor, false);
    }

    public void setOnContactRemovalListener(OnContactRemovalListener listener) {
        mListener = listener;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.contactDisplayName.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
        holder.contactTelephoneNumber.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        holder.contactRemoveButton.setTag(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));
        
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
        
        holder.contactRemoveButton = (ImageView) view.findViewById(R.id.contact_remove_button);
        holder.contactRemoveButton.setOnClickListener(mClickListener);
        
        view.setTag(holder);
        return view;
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
