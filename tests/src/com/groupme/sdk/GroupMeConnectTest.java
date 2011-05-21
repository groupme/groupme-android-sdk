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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;

import com.groupme.sdk.mock.GroupMeContext;

public class GroupMeConnectTest extends AndroidTestCase {
    GroupMeConnect mConnect;

    @Override
    protected void setUp() throws Exception {
        mConnect = GroupMeConnect.getInstance(getContext());
        mConnect.setAppIdWithSecret("sample-app-id", "sample-app-secret");
        mConnect.setDefaultGroupName("default-group");
        
        assertNotNull(mConnect);
    }

    public void testBuildObject() {
        assertEquals(mConnect.mContext, getContext());
        assertEquals(mConnect.mAppId, "sample-app-id");
        assertEquals(mConnect.mSecret, "sample-app-secret");
        assertEquals(mConnect.getDefaultGroupName(), "default-group");
    }

    public void testHasGroupMeAppInstalled() {
        PackageManager pkgManager = getContext().getPackageManager();
        boolean isAppInstalled;

        try {
            PackageInfo info = pkgManager.getPackageInfo(GroupMeConnect.GROUP_ME_APP_PACKAGE, 0);
            isAppInstalled = info.packageName.equals(GroupMeConnect.GROUP_ME_APP_PACKAGE);
        } catch (PackageManager.NameNotFoundException e) {
            isAppInstalled = false;
        }

        assertEquals(mConnect.hasGroupMeAppInstalled(), isAppInstalled);
    }

    public void testLaunchAndroidMarket() {
        GroupMeContext context = new GroupMeContext();
        context.setRequestType(GroupMeContext.REQUEST_DOWNLOAD_APP);

        mConnect.setContext(context);
        mConnect.setAppIdWithSecret("sample-app-id", "sample-app-secret");
        
        mConnect.downloadGroupMeApp();
    }
}
