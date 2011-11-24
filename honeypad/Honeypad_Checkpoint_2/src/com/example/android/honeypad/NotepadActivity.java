package com.example.android.honeypad;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class NotepadActivity extends FragmentActivity {

	// key for adding NoteEditFragment to this Activity
	private static final String NOTE_LIST_TAG = "List";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
		if ( null == savedInstanceState ) {
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
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
}
