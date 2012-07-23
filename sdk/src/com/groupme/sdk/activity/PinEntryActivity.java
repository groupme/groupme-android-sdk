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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.groupme.sdk.GroupMeConnect;
import com.groupme.sdk.GroupMeRequest;
import com.groupme.sdk.R;
import com.groupme.sdk.util.Constants;

public class PinEntryActivity extends Activity implements GroupMeRequest.RequestListener {
    public static final int DIALOG_LOADING = 0x1;

    public static final int VERIFY_PIN = 0x1;

    public static final int PIN_VERIFIED = 0x1;
    public static final int PIN_VERIFICATION_FAILED = 0x2;
    public static final int NUMBER_REENTER = 0x3;

    Button mAlternateButton;
	EditText mNameEntry;
    EditText mPinEntry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinentry);

        TextView pinEntryInstructions = (TextView) findViewById(R.id.pin_entry_instructions);
        pinEntryInstructions.setText(getString(R.string.pin_entry_instructions, getIntent().getStringExtra("phone_number")));

		mNameEntry = (EditText) findViewById(R.id.name_entry);
        mPinEntry = (EditText) findViewById(R.id.pin_entry);
        mAlternateButton = (Button) findViewById(R.id.not_number_button);

        SpannableString str = new SpannableString(getString(R.string.link_not_number));
        str.setSpan(new UnderlineSpan(), 0, str.length(), 0);
        mAlternateButton.setText(str);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        return onCreateDialog(id, null);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DIALOG_LOADING:
            ProgressDialog dialog = new ProgressDialog(this);
    		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		dialog.setMessage(getString(R.string.validation_waiting_message));
    		return dialog;
        default:
            throw new IllegalArgumentException("Unknown dialog id: " + id);
        }
    }

    public void onValidateButtonClick(View view) {
        GroupMeConnect connect = GroupMeConnect.getInstance(this);
        connect.validatePinNumber(getIntent().getStringExtra("phone_number"), mPinEntry.getText().toString(), this);

        showDialog(DIALOG_LOADING);
    }

    public void onAlternateNumberClick(View view) {
        SharedPreferences preferences = getSharedPreferences("groupme.prefs", MODE_PRIVATE);
        preferences.edit().putBoolean("needs.pin", false).commit();
        preferences.edit().putString("phone.number", "").commit();

        setResult(NUMBER_REENTER);
        finish();
    }

    /* GroupMeRequest.RequestListener methods. */

    public void onRequestStarted(GroupMeRequest request) {

    }

    public void onRequestFailed(GroupMeRequest request) {
        setResult(PIN_VERIFICATION_FAILED);
        finish();
    }

    public void onRequestCompleted(GroupMeRequest request) {
        if (request.getRequest().equals(GroupMeRequest.CLIENT_AUTHORIZE)) {
			setUserName(request);
		} else {
			completeAuth(request);
		}
    }

	private void setUserName(GroupMeRequest request) {
		try {
            JSONObject response = new JSONObject(request.getResponse()).getJSONObject("response");
            String userId = response.getString("user_id");
            String token = response.getString("access_token");

            SharedPreferences preferences = getSharedPreferences("groupme.prefs", MODE_PRIVATE);
            preferences.edit().putString("user_id", userId).putString("token", token).commit();

			GroupMeConnect connect = GroupMeConnect.getInstance(this);
			connect.setMemberName(userId, mNameEntry.getText().toString(), this);
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Invalid response from server: " + e.toString());
        }
	}
	
	private void completeAuth(GroupMeRequest request) {
		removeDialog(DIALOG_LOADING);

        setResult(PIN_VERIFIED);
        finish();
	}
}
