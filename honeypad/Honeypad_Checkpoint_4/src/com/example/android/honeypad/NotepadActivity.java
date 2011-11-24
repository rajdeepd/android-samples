package com.example.android.honeypad;

import com.example.android.honeypad.NoteListFragment.NoteListEventsCallback;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class NotepadActivity extends FragmentActivity implements
		NoteListEventsCallback {

	// key for adding NoteEditFragment to this Activity
	private static final String NOTE_EDIT_TAG = "Edit";
	private static final String NOTE_LIST_TAG = "List";

	private boolean mUseMultiplePanes;
	private boolean mIsV11 = Build.VERSION.SDK_INT >= 11;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
		mUseMultiplePanes = (null != findViewById(R.id.note_detail_container));
		if (null == savedInstanceState) {
			FragmentManager fm = getSupportFragmentManager();
			NoteListFragment nlf = new NoteListFragment();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.list, nlf, NOTE_LIST_TAG);
			ft.commit();
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
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
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
