package com.example.android.honeypad;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.honeypad.NoteListFragment.InitListView;
import com.example.android.honeypad.widget.WidgetProvider;

public class NodeSelectionModeCallback implements ListView.MultiChoiceModeListener, InitListView {
	final private NoteListFragment mLF;
	public NodeSelectionModeCallback( NoteListFragment lf ) {
		mLF = lf;
	}
	
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mLF.getActivity().getMenuInflater();
		inflater.inflate(R.menu.notes_list_context, menu);
		return true;
	}

	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		updateTitleLabel(mode);
		return true;
	}

	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_notes:
			int deletedCount = 0;

			for (long id : mLF.getListView().getCheckItemIds()) {
				deletedCount += mLF.getActivity().getContentResolver().delete(
						ContentUris.withAppendedId(
								NotesProvider.CONTENT_URI, id), null, null);
			}

			// clear any selections
			mLF.clearActivation();

			// update container
			mLF.mContainerCallback.onNoteDeleted();

			// show a toast to confirm delete
			Toast.makeText(
					mLF.getActivity(),
					String.format(
							mLF.getActivity().getString(R.string.num_deleted),
							deletedCount, (deletedCount == 1 ? "" : "s")),
					Toast.LENGTH_SHORT).show();

			// update widget
			AppWidgetManager awm = AppWidgetManager
					.getInstance(mLF.getActivity());
			awm.notifyAppWidgetViewDataChanged(awm
					.getAppWidgetIds(new ComponentName(mLF.getActivity(),
							WidgetProvider.class)), R.id.stack_view);

			// clear the contextual action bar
			mode.finish();
		}
		return true;
	}

	void updateTitleLabel(ActionMode mode) {
		// update the label to show the number of items selected
		mode.setTitle(String.format(
				mLF.getActivity().getString(R.string.num_selected),
				mLF.getListView().getCheckedItemCount()));			
	}
	
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		updateTitleLabel(mode);
	}

	public void onDestroyActionMode(ActionMode mode) {
	}

	@Override
	public void init(ListView lv) {
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(this);
	}
}
