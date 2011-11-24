package com.example.android.honeypad;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class NotepadActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.notepad_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
}
