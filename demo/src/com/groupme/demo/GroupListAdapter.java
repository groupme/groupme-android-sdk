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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.groupme.sdk.GroupMeGroup;

import java.util.List;

public class GroupListAdapter extends BaseAdapter {
    private Context mContext;
    private List<GroupMeGroup> mGroups;

    public GroupListAdapter(Context context, List<GroupMeGroup> groups) {
        mGroups = groups;
        mContext = context;
    }

    public int getCount() {
        return mGroups.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public GroupMeGroup getItem(int position) {
        return mGroups.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_group, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.groupNameView = (TextView) convertView.findViewById(R.id.group_name_view);
            holder.groupDescriptionView = (TextView) convertView.findViewById(R.id.group_description_view);

            convertView.setTag(holder);
        }

        GroupMeGroup group = mGroups.get(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.groupNameView.setText(group.getTopic());
        holder.groupDescriptionView.setText(group.getDescription());
        
        return convertView;
    }

    static class ViewHolder {
        TextView groupNameView;
        TextView groupDescriptionView;
    }
}
