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

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.example.android.honeypad.NoteListFragment.NoteListEventsCallback;

public class NotepadActivity extends FragmentActivity implements
		NoteListEventsCallback {

	// action to launch straight to a specific note
	public static final String ACTION_VIEW_NOTE = "com.example.android.honeypad.ACTION_VIEW_NOTE";

	// extra for the above action
	public static final String EXTRA_NOTE_ID = "noteId";

	// key for adding NoteEditFragment to this Activity
	private static final String NOTE_EDIT_TAG = "Edit";
	private static final String NOTE_LIST_TAG = "List";
	
	private Intent mNewIntent;

	private boolean mUseMultiplePanes;
	private boolean mIsV11 = Build.VERSION.SDK_INT >= 11;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
		mUseMultiplePanes = ( null != findViewById(R.id.note_detail_container) );
		if ( null == savedInstanceState ) {
			FragmentManager fm = getSupportFragmentManager();
			NoteListFragment nlf = new NoteListFragment();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.list, nlf, NOTE_LIST_TAG);
			ft.commit();
			if (ACTION_VIEW_NOTE.equals(getIntent().getAction())) {
				viewNote(getIntent(), nlf);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mNewIntent = intent;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = mNewIntent;
		if ( null != intent ) {
			mNewIntent = null;
			if (ACTION_VIEW_NOTE.equals(intent.getAction())) {
				NoteListFragment list = (NoteListFragment) getSupportFragmentManager()
				.findFragmentByTag(NOTE_LIST_TAG);
				if ( null != list ) {
					viewNote(intent, list);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.notepad_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_note:
			showNote(null);
			NoteListFragment list = (NoteListFragment) getSupportFragmentManager()
					.findFragmentByTag(NOTE_LIST_TAG);
			if ( null != list ) {
				list.clearActivation();
			}
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void viewNote(Intent launchIntent, NoteListFragment list) {
		final long noteId = launchIntent.getLongExtra(EXTRA_NOTE_ID, -1);
		showNote(ContentUris.withAppendedId(NotesProvider.CONTENT_URI, noteId));
		list.setActivatedNote(noteId);
	}

	void fillNote(Uri noteUri, boolean editNoteAdded) {
		NoteEditFragment editFrag = (NoteEditFragment) getSupportFragmentManager()
				.findFragmentByTag(NOTE_EDIT_TAG);
		if (noteUri != null) {
			// load an existing note
			editFrag.loadNote(noteUri);
		} else {
			// creating a new note - clear the form & list
			// activation
			if (editNoteAdded) {
				editFrag.clear();
			}
			NoteListFragment list = (NoteListFragment) getSupportFragmentManager()
					.findFragmentByTag(NOTE_LIST_TAG);
			if ( null != list ) {
				list.clearActivation();
			}
		}

	}

	interface NoteTransitionAnimatorInterface {
		void startAnimation(NoteEditFragment edit,
				NotepadActivity notepadActivity, final Uri noteUri,
				final boolean editNoteAdded);
	}

	/**
	 * 
	 * This method controls both fragments, instructing them to display a
	 * certain note.
	 * 
	 * @param noteUri
	 *            The {@link Uri} of the note to show. To create a new note,
	 *            pass {@code null}.
	 */
	private void showNote(final Uri noteUri) {
		// check if the NoteEditFragment has been added
		FragmentManager fm = getSupportFragmentManager();
		NoteEditFragment edit = (NoteEditFragment) fm
				.findFragmentByTag(NOTE_EDIT_TAG);
		final boolean editNoteAdded = (edit != null);

		if (editNoteAdded) {
			if (edit.mCurrentNote != null && edit.mCurrentNote.equals(noteUri)) {
				// clicked on the currently selected note
				return;
			}
			
			if ( mIsV11 ) {
				NoteTransitionAnimatorInterface ntai = new NoteTransitionAnimator();
				ntai.startAnimation(edit, this, noteUri, editNoteAdded);
			} else {
				fillNote(noteUri, editNoteAdded);				
			}
				
			
		} else {
			// add the NoteEditFragment to the container
			FragmentTransaction ft = fm.beginTransaction();
			edit = new NoteEditFragment();
			if (mUseMultiplePanes) {
				ft.add(R.id.note_detail_container, edit, NOTE_EDIT_TAG);
			} else {
				Bundle b = new Bundle();
				b.putBoolean(NoteEditFragment.ARGUMENT_POP_ON_SAVE, true);
				edit.setArguments(b);
				ft.replace(R.id.list, edit, NOTE_EDIT_TAG);
			    ft.addToBackStack(null);			
		    }
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.commit();
			edit.loadNote(noteUri);
		}
	}

	/**
	 * Callback from child fragment
	 */
	@Override
	public void onNoteSelected(Uri noteUri) {
		showNote(noteUri);
	}

	/**
	 * Callback from child fragment
	 */
	@Override
	public void onNoteDeleted() {
		// remove the NoteEditFragment after a deletion
		FragmentManager fm = getSupportFragmentManager();
		NoteEditFragment edit = (NoteEditFragment) fm
				.findFragmentByTag(NOTE_EDIT_TAG);
		if (edit != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(edit);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}
	}
}
