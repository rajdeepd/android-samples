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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class NoteListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnClickListener,
		AnimationListener {

	private static final int DELETE_ID = Menu.FIRST + 1;

	public static final String CHECKED_ITEMS_KEY = "CHK";

	private Cursor mNotesCursor;
	private View mMultiSelectPanel;
	private Button mDeleteButton;
	private InitListView mNodeSelectionModeCallback;
	private ListView mLV;
	static private final String[] DEFAULT_PROJECTION = new String[] {
			NotesProvider.KEY_ID, NotesProvider.KEY_TITLE };
	static private final String DEFAULT_SORT = NotesProvider.KEY_TITLE
			+ " COLLATE LOCALIZED ASC";
	private boolean mIsV11 = Build.VERSION.SDK_INT >= 11;

	// containing Activity must implement this interface
	public interface NoteListEventsCallback {
		public void onNoteSelected(Uri noteUri);
		public void onNoteDeleted();
	}

	// key for saving state
	private static final String KEY_CURRENT_ACTIVATED = "KEY_CURRENT_ACTIVATED";

	// the id of our loader
	private static final int LOADER_ID = 0;

	// This is the Adapter being used to display the list's data.
	private CursorAdapter mAdapter;

	// callback for notifying container of events
	NoteListEventsCallback mContainerCallback;

	// track the currently activated item
	private int mCurrentActivePosition = ListView.INVALID_POSITION;

	// track if we need to set a note to activated once data is loaded
	private long mNoteIdToActivate = -1;

	interface InitListView {
		void init(ListView lv);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.note_list, null);
		mMultiSelectPanel = v.findViewById(R.id.footer_organize);
		mDeleteButton = (Button) v.findViewById(R.id.btn_multi_delete);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final ListView notesList = getListView();
		if (mIsV11) {
			mNodeSelectionModeCallback = new NodeSelectionModeCallback(this);
		} else {
			registerForContextMenu(notesList);
		}

		// create an empty adapter, our Loader will retrieve the data
		// asynchronously

		mAdapter = new ActivatedCursorAdapter(this.getActivity(), mNotesCursor);
		setListAdapter(mAdapter);

		// setup our list view
		mLV = notesList;
		if (null != mNodeSelectionModeCallback) {
			mNodeSelectionModeCallback.init(notesList);
		} else {
			notesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}

		// restore any saved state
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_CURRENT_ACTIVATED)) {
				mCurrentActivePosition = savedInstanceState.getInt(
						KEY_CURRENT_ACTIVATED, ListView.INVALID_POSITION);
			}
		}

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_ID, null, this);
		// setEmptyText(getActivity().getString(R.string.no_notes));

		mDeleteButton.setOnClickListener(this);
	}

	public int getCheckedItemCount() {
		if (mIsV11) {
			return mLV.getCheckedItemCount();
		} else {
			/** Slow count **/
			SparseBooleanArray sba = mLV.getCheckedItemPositions();
			int count = 0;
			int size = sba.size();
			for (int i = 0; i < size; i++) {
				int key = sba.keyAt(i);
				if (sba.get(key)) {
					count++;
				}
			}
			return count;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			// check that the containing activity implements our callback
			mContainerCallback = (NoteListEventsCallback) activity;
		} catch (ClassCastException e) {
			activity.finish();
			throw new ClassCastException(activity.toString()
					+ " must implement NoteSelectedCallback");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CURRENT_ACTIVATED, mCurrentActivePosition);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Helper method to set the activation state of a note
	 * 
	 * @param noteId
	 *            The id of the note to be activated
	 */
	protected void setActivatedNote(long noteId) {
		if (mAdapter != null) {
			// work out the position in the list of note with the given id
			final int N = mAdapter.getCount();
			for (int position = 0; position < N; position++) {
				if (mAdapter.getItemId(position) == noteId) {
					if (position != mCurrentActivePosition) {
						clearActivation();
						mCurrentActivePosition = position;
						View row = getListView().getChildAt(position);
						if (row != null) {
							row.setActivated(true);
						}
					}
					break;
				}
			}
		} else {
			// if we have not loaded our cursor yet then store the note id
			// for now & activate once loaded
			mNoteIdToActivate = noteId;
		}				
	}

	/**
	 * Helper method to clear the list's activated state
	 */
	protected void clearActivation() {
		if (mIsV11) {
			if (mCurrentActivePosition != ListView.INVALID_POSITION) {
				getListView().getChildAt(mCurrentActivePosition).setActivated(
						false);
			}
		}
		mCurrentActivePosition = ListView.INVALID_POSITION;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
				DEFAULT_PROJECTION, null, null, DEFAULT_SORT);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
		// check if we need to set one of the (now loaded) notes as activated
		if (mNoteIdToActivate > -1) {
			setActivatedNote(mNoteIdToActivate);
			mNoteIdToActivate = -1;
		}
		showMultiPanel();
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (!mIsV11) {
			/*
			 * Slight Hack: Multiselect lists are automatically
			 * Checked/unchecked. If the item was previously checked, it will
			 * now be unchecked, etc. We compensate for that here.
			 */
			l.setItemChecked(position, !l.isItemChecked(position));
		}
		mCurrentActivePosition = position;
		mContainerCallback.onNoteSelected(ContentUris.withAppendedId(
				NotesProvider.CONTENT_URI, id));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			getListView().setItemChecked(info.position, false);
			getActivity().getContentResolver().delete(
					ContentUris.withAppendedId(NotesProvider.CONTENT_URI,
							info.id), null, null);			
			// clear any selections
			mLV.clearChoices();
			
			// update container
			mContainerCallback.onNoteDeleted();
			
			// show a toast to confirm delete
			Toast.makeText(
					getActivity(),
					String.format(
							getString(R.string.num_deleted),
							1, ""),
					Toast.LENGTH_SHORT).show();			
			
			showMultiPanel();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);

	}

	/**
	 * 
	 * A trivial extension to {@link SimpleCursorAdapter} that sets a specified
	 * item's Activated state.
	 * 
	 */
	private class ActivatedCursorAdapter extends CursorAdapter {

		public ActivatedCursorAdapter(Context context, Cursor cur) {
			super(context, cur, false);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			View cbview = (View) v.getTag(R.id.checkbox);
			if (null != cbview) {
				cbview.setTag(R.id.list, position);
				if (mIsV11) {
					boolean isActivated = position == mCurrentActivePosition;
					v.setActivated(isActivated);
				}
			}
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView iv = (ImageView) view.getTag(R.id.checkbox);
			TextView tv = (TextView) view.getTag(R.id.text);
			tv.setText(cursor.getString(1));
			int itemId = cursor.getInt(0);
			iv.setTag(R.id.body, itemId);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = getActivity().getLayoutInflater().inflate(
					R.layout.note_list_row, null);
			ImageView iv = (ImageView) v.findViewById(R.id.checkbox);
			TextView tv = (TextView) v.findViewById(R.id.text);
			iv.setOnClickListener(NoteListFragment.this);
			v.setTag(R.id.checkbox, iv);
			v.setTag(R.id.text, tv);
			return v;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkbox:
			Integer position = (Integer) v.getTag(R.id.list);
			if (null != position) {
				ListView lv = getListView();
				int pos = position;
				boolean isChecked = !lv.isItemChecked(pos);
				lv.setItemChecked(pos, isChecked);
				if (!mIsV11) {
					showMultiPanel();
				}
			}
			break;
		case R.id.btn_multi_delete: {
			Activity a = getActivity();
			ContentResolver cr = a.getContentResolver();
			ListView lv = mLV;
			int deletedCount = 0;
			/** Badness. Don't loop a delete operation on the UI thread **/
			/** We also assume that the ID's and positions are constant **/
			for (long id : lv.getCheckItemIds()) {
				deletedCount += cr.delete(ContentUris.withAppendedId(
						NotesProvider.CONTENT_URI, id), null, null);
			}
			// clear any selections
			lv.clearChoices();

			// update container
			mContainerCallback.onNoteDeleted();

			// show a toast to confirm delete
			Toast.makeText(
					a,
					String.format(
							getString(R.string.num_deleted),
							deletedCount, (deletedCount == 1 ? "" : "s")),
					Toast.LENGTH_SHORT).show();
			showMultiPanel();
			break;
		}
		}
	}

	private void updateListPosition() {
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		updateListPosition();
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {

	}

	/**
	 * Show or hide the panel of multi-select options
	 */
	private void showMultiPanel() {
		if (mIsV11) {
		} else {
			int count = getCheckedItemCount();
			boolean show = count != 0;
			if (show && mMultiSelectPanel.getVisibility() != View.VISIBLE) {
				mMultiSelectPanel.setVisibility(View.VISIBLE);
				Animation animation = AnimationUtils.loadAnimation(
						getActivity(), R.anim.footer_appear);
				animation.setAnimationListener(this);
				mMultiSelectPanel.startAnimation(animation);
			} else if (!show && mMultiSelectPanel.getVisibility() != View.GONE) {
				mMultiSelectPanel.setVisibility(View.GONE);
				mMultiSelectPanel.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), R.anim.footer_disappear));
			}
			switch (count) {
			case 0:
				break;
			case 1:
				mDeleteButton.setText(R.string.menu_delete);
				break;
			default:
				mDeleteButton.setText(getResources().getString(
						R.string.menu_delete_num, count));
				break;
			}
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

}
