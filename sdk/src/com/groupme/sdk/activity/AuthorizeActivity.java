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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.groupme.sdk.GroupMeConnect;
import com.groupme.sdk.GroupMeRequest;
import com.groupme.sdk.R;
import com.groupme.sdk.util.Constants;
import com.groupme.sdk.util.HttpUtils;

public class AuthorizeActivity extends Activity implements GroupMeRequest.RequestListener {
    public static final int DIALOG_LOADING = 0x1;
    public static final int DIALOG_INVALID_NUMBER = 0x2;
    public static final int DIALOG_NETWORK_ERROR = 0x3;
    
    public static final int REQUEST_LOGIN = 0x20202;
    public static final int LOGIN_SUCCESSFUL = 0x3333;
    public static final int LOGIN_FAILED = 0x4444;
    public static final int LOGIN_CANCELLED = 0x5555;
    
    EditText mPhoneNumberEntry;
    PhoneNumberFormattingTextWatcher mPhoneWatcher = new PhoneNumberFormattingTextWatcher();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);

        mPhoneNumberEntry = (EditText) findViewById(R.id.phone_number_entry);
        mPhoneNumberEntry.addTextChangedListener(mPhoneWatcher);
        
        SharedPreferences preferences = getSharedPreferences("groupme.prefs", MODE_PRIVATE);

        if (preferences.getBoolean("needs.pin", false)) {
            Intent intent = new Intent(this, PinEntryActivity.class);
            intent.putExtra("phone_number", preferences.getString("phone.number", ""));

            startActivityForResult(intent, PinEntryActivity.VERIFY_PIN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == PinEntryActivity.PIN_VERIFIED) {
            setResult(LOGIN_SUCCESSFUL);
            finish();
        }

        if (resultCode == PinEntryActivity.PIN_VERIFICATION_FAILED) {
            setResult(LOGIN_FAILED);
            finish();
        }

        if (resultCode != PinEntryActivity.NUMBER_REENTER) {
            setResult(LOGIN_CANCELLED);
            finish();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog.Builder builder;

        switch (id) {
        case DIALOG_LOADING:
            ProgressDialog dialog = new ProgressDialog(this);
    		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		dialog.setMessage(getString(R.string.authorize_waiting_message));
    		return dialog;
        case DIALOG_INVALID_NUMBER:
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.invalid_number_title));
            builder.setMessage(getString(R.string.invalid_number_message));
            builder.setNeutralButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        case DIALOG_NETWORK_ERROR:
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_network_error_title));
            builder.setMessage(getString(R.string.dialog_network_error_desc));
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

    public void onPhoneButtonClick(View view) {
        Log.d(Constants.LOG_TAG, "Phone number field: " + mPhoneNumberEntry.getText().toString());
        
        if (!validatePhoneNumber(mPhoneNumberEntry.getText().toString())) {
            showDialog(DIALOG_INVALID_NUMBER);
            return;
        }

        PackageManager pkgManager = getPackageManager();
        int access = pkgManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, getPackageName());

        if (access == PackageManager.PERMISSION_GRANTED) {
            if (!HttpUtils.isConnectedToNetwork(this)) {
                showDialog(DIALOG_NETWORK_ERROR);
                return;
            }
        }

        GroupMeConnect connect = GroupMeConnect.getInstance(this);
        connect.authorizePhoneNumber(mPhoneNumberEntry.getText().toString(), this);

        showDialog(DIALOG_LOADING);
    }

    public boolean validatePhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber);
    }

    /* GroupMeRequest.RequestListener methods. */

    public void onRequestStarted(GroupMeRequest request) {

    }

    public void onRequestFailed(GroupMeRequest request) {
        /* show error dialog. */
        removeDialog(DIALOG_LOADING);
        showDialog(DIALOG_NETWORK_ERROR);
    }

    public void onRequestCompleted(GroupMeRequest request) {
        SharedPreferences preferences = getSharedPreferences("groupme.prefs", MODE_PRIVATE);
        
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra("phone_number", mPhoneNumberEntry.getText().toString());

        preferences.edit().putBoolean("needs.pin", true).commit();
        preferences.edit().putString("phone.number", mPhoneNumberEntry.getText().toString()).commit();

        mPhoneNumberEntry.setText("");
        
        removeDialog(DIALOG_LOADING);
        startActivityForResult(intent, PinEntryActivity.VERIFY_PIN);
    }
}
