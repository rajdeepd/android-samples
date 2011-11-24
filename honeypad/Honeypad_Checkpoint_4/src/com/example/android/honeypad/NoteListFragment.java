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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class NoteListFragment extends android.support.v4.app.ListFragment implements OnClickListener,
		AnimationListener {
	private static final int DELETE_ID = Menu.FIRST + 1;

	public static final String CHECKED_ITEMS_KEY = "CHK";

	private Cursor mNotesCursor;
	private View mMultiSelectPanel;
	private Button mDeleteButton;
	private ListView mLV;
	// callback for notifying container of events
	NoteListEventsCallback mContainerCallback;

    // containing Activity must implement this interface
    public interface NoteListEventsCallback {
        public void onNoteSelected(Uri noteUri);
        public void onNoteDeleted();
    }
	
	static private final String[] DEFAULT_PROJECTION = new String[] {
			NotesProvider.KEY_ID, NotesProvider.KEY_TITLE };
	static private final String DEFAULT_SORT = NotesProvider.KEY_TITLE
			+ " COLLATE LOCALIZED ASC";
	
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
		notesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        registerForContextMenu(notesList);
		mLV = notesList;
		mDeleteButton.setOnClickListener(this);
		fillData();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		showMultiPanel();
	}

	public int getCheckedItemCount() {
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

	private void fillData() {
		// Get all of the rows from the database and create the item list
		mNotesCursor = getActivity().getContentResolver().query(NotesProvider.CONTENT_URI,
				DEFAULT_PROJECTION, null, null, DEFAULT_SORT);
		getActivity().startManagingCursor(mNotesCursor);

		final LayoutInflater li = getActivity().getLayoutInflater();

		// Now create a simple cursor adapter and set it to display
		CursorAdapter notes = new CursorAdapter(getActivity(), mNotesCursor) {

			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v =  super.getView(position, convertView, parent);
				ImageView iv = (ImageView) v.getTag(R.id.checkbox);
				iv.setTag(position);
				return v;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				ImageView iv = (ImageView) view.getTag(R.id.checkbox);
				TextView tv = (TextView) view.getTag(R.id.text);
				tv.setText(cursor.getString(1));
				int itemId = cursor.getInt(0);
				iv.setTag(itemId);
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View v = li.inflate(R.layout.note_list_row, null);
				ImageView iv = (ImageView) v.findViewById(R.id.checkbox);
				TextView tv = (TextView) v.findViewById(R.id.text);
				iv.setOnClickListener(NoteListFragment.this);
				v.setTag(R.id.checkbox, iv);
				v.setTag(R.id.text, tv);
				return v;
			}

		};
		setListAdapter(notes);
		showMultiPanel();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			getActivity().getContentResolver().delete(
					ContentUris.withAppendedId(NotesProvider.CONTENT_URI,
							info.id), null, null);			
			// clear any selections
			mLV.clearChoices();
			
			// update container
			mContainerCallback.onNoteDeleted();
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		/*
		 * Slight Hack: Multiselect lists are automatically
		 * Checked/unchecked. If the item was previously checked, it will
		 * now be unchecked, etc. We compensate for that here.
		 */
		l.setItemChecked(position, !l.isItemChecked(position));
		mContainerCallback.onNoteSelected(ContentUris.withAppendedId(
				NotesProvider.CONTENT_URI, id));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			fillData();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkbox:
			Integer position = (Integer) v.getTag();
			if (null != position) {
				ListView lv = getListView();
				int pos = position;
				boolean isChecked = !lv.isItemChecked(pos);
				lv.setItemChecked(pos, isChecked);
				showMultiPanel();
			}
			break;
		case R.id.btn_multi_delete:
			/** Badness. Don't loop a delete operation on the UI thread **/
			for (long id : mLV.getCheckItemIds()) {
				getActivity().getContentResolver().delete(
						ContentUris.withAppendedId(NotesProvider.CONTENT_URI,
								id), null, null);
			}
			// clear any selections
			mLV.clearChoices();
			
			// update container
			mContainerCallback.onNoteDeleted();

			showMultiPanel();
			break;
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
		int count = getCheckedItemCount();
		boolean show = count != 0;
		if (show && mMultiSelectPanel.getVisibility() != View.VISIBLE) {
			mMultiSelectPanel.setVisibility(View.VISIBLE);
			Animation animation = AnimationUtils.loadAnimation(getActivity(),
					R.anim.footer_appear);
			animation.setAnimationListener(this);
			mMultiSelectPanel.startAnimation(animation);
		} else if (!show && mMultiSelectPanel.getVisibility() != View.GONE) {
			mMultiSelectPanel.setVisibility(View.GONE);
			mMultiSelectPanel.startAnimation(AnimationUtils.loadAnimation(getActivity(),
					R.anim.footer_disappear));
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

	@Override
	public void onAnimationStart(Animation animation) {

	}
}