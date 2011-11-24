/*
 * Copyright (C) 2011 Google Inc.
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

package com.example.android.honeypad;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.honeypad.widget.WidgetProvider;

public class NoteEditFragment extends Fragment {

	private EditText mTitleText;
	private EditText mBodyText;
	private boolean mIsV11 = Build.VERSION.SDK_INT >= 11;	
	public static String ARGUMENT_POP_ON_SAVE = "POPS";

	// expose the currently displayed note
	protected Uri mCurrentNote;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.note_edit, container, false);

		mTitleText = (EditText) v.findViewById(R.id.title);
		mBodyText = (EditText) v.findViewById(R.id.body);
		Button confirmButton = (Button) v.findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				saveNote();
			}
		});

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(NotesProvider.KEY_ID)) {
			mCurrentNote = Uri.parse((String) savedInstanceState
					.getString(NotesProvider.KEY_ID));
		}
		populateFields();
		return v;
	}

	/**
	 * Display a particular note in this fragment.
	 * 
	 * @param noteUri
	 *            The Uri of the note to display
	 */
	protected void loadNote(Uri noteUri) {
		mCurrentNote = noteUri;
		if (isAdded()) {
			populateFields();
		}
	}

	/**
	 * Clear all fields on this fragment.
	 */
	protected void clear() {
		mTitleText.setText(null);
		mBodyText.setText(null);
		mCurrentNote = null;
	}
	
	
	/**
	 * Helper method which retrieves & displays the content of the current note.
	 */
	private void populateFields() {
		if (mCurrentNote != null) {

			Cursor c = null;
			try {
				c = getActivity().getContentResolver().query(mCurrentNote,
						null, null, null, null);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mCurrentNote != null) {
			outState.putString(NotesProvider.KEY_ID, mCurrentNote.toString());
		}
	}

	/**
	 * Persists the details of the current note. This will either create a new
	 * note, or update an existing note.
	 */
	private void saveNote() {
		ContentValues values = new ContentValues(2);
		values.put(NotesProvider.KEY_TITLE, mTitleText.getText().toString());
		values.put(NotesProvider.KEY_BODY, mBodyText.getText().toString());
		final boolean updating = mCurrentNote != null;
		if (updating) {
			getActivity().getContentResolver().update(mCurrentNote, values,
					null, null);
		} else {
			Uri newNote = getActivity().getContentResolver().insert(
					NotesProvider.CONTENT_URI, values);

			if (newNote != null) {
				mCurrentNote = newNote;
			}
		}

		// show a toast confirmation
		Toast.makeText(getActivity(),
				updating ? R.string.note_updated : R.string.note_saved,
				Toast.LENGTH_SHORT).show();

		if ( mIsV11 ) {
			// update widget
			AppWidgetManager awm = AppWidgetManager.getInstance(getActivity());
	
			awm.notifyAppWidgetViewDataChanged(awm
					.getAppWidgetIds(new ComponentName(getActivity(),
							WidgetProvider.class)), R.id.stack_view);			
		}
		Toast.makeText(getActivity(), "Note Saved", Toast.LENGTH_SHORT).show();
		Bundle b = getArguments();
		if ( null != b && b.containsKey(ARGUMENT_POP_ON_SAVE) ) {
			getFragmentManager().popBackStack();
		}		
	}

}
