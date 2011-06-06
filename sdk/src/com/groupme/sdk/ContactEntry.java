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
package com.groupme.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactEntry implements Parcelable {
    private String mName;
    private String mNumber;

    public ContactEntry() {

    }

    public ContactEntry(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return mName;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mNumber);
    }

    public void readFromParcel(Parcel in) {
        mName = in.readString();
        mNumber = in.readString();
    }

    public int describeContents() {
        return 0;
    }
    
    public static final Creator<ContactEntry> CREATOR = new Creator<ContactEntry>() {
        public ContactEntry createFromParcel(Parcel in) {
            return new ContactEntry(in);
        }

        public ContactEntry[] newArray(int size) {
            return new ContactEntry[size];
        }
    };

}
