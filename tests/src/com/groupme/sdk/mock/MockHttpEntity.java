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
package com.groupme.sdk.mock;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

public class MockHttpEntity implements HttpEntity {
    Context mContext;
    String mFile;
    InputStream mStream;

    public MockHttpEntity(Context context, String file) {
        mContext = context;
        mFile = file;
    }

    public void consumeContent() throws IOException {
        try {
            mStream = mContext.getPackageManager().getResourcesForApplication("com.groupme.sdk.tests")
                    .getAssets().open(String.format("http/%s", mFile));
        } catch (PackageManager.NameNotFoundException e) {
            throw new IOException("Cannot locate package containing files: " + e.toString());
        }
    }

    public InputStream getContent() {
        return mStream;
    }

    public Header getContentEncoding() {
        return new BasicHeader("Content-Encoding", "UTF-8");
    }

    public long getContentLength() {
        try {
            if (mStream != null) {
                return mStream.available();
            } else {
                return -1;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    public Header getContentType() {
        return new BasicHeader("Content-Type", "text/json");
    }

    public boolean isChunked() {
        return false;
    }

    public boolean isRepeatable() {
        return false;
    }

    public boolean isStreaming() {
        return true;
    }

    public void writeTo(OutputStream stream) {

    }
}
