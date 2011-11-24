/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.android.honeypad;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NoteEdit extends Activity {

	private EditText mTitleText;
	private EditText mBodyText;
	private Uri mCurrentNote;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		setTitle(R.string.edit_note);
		mTitleText = (EditText) findViewById(R.id.title);
		mBodyText = (EditText) findViewById(R.id.body);

		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				saveNote();
				setResult(RESULT_OK);
				finish();
			}
		});

		mCurrentNote = getIntent().getData();
		populateFields();
	}

	private void populateFields() {
		if (mCurrentNote != null) {
			Cursor c = null;
			try {
				c = getContentResolver().query(mCurrentNote, null, null, null,
						null);
				if (c.moveToFirst()) {
					mTitleText.setText(c.getString(NotesProvider.TITLE_COLUMN));
					mBodyText.setText(c.getString(NotesProvider.BODY_COLUMN));
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}
		}
	}

	private void saveNote() {
		// save/update the note
		ContentValues values = new ContentValues(2);
		values.put(NotesProvider.KEY_TITLE, mTitleText.getText().toString());
		values.put(NotesProvider.KEY_BODY, mBodyText.getText().toString());
		if (mCurrentNote != null) {
			getContentResolver().update(mCurrentNote, values, null, null);
		} else {
			getContentResolver().insert(NotesProvider.CONTENT_URI, values);
		}
		Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
	}
}
