package com.android.example.contact;

import com.android.example.contact.task.ListProfileTask;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	static final String TAG = MainActivity.class.getName();
	ActionBar actionBar;
	TabListener tabListener;
	Tab profileTab;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        actionBar = getActionBar();    
        actionBar.setDisplayShowTitleEnabled(false);
        tabListener = new TabListener(this);
        
        profileTab = actionBar.newTab().setText("Profile").setTabListener(tabListener);
        actionBar.addTab(profileTab);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Log.i("",""+Thread.currentThread().getId());
    }
    class TabListener implements ActionBar.TabListener {
    	String lastTab = null;
    	private Activity activity;
    	public TabListener(Activity activity) {
    		this.activity = activity;
    	}
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        Log.i("",""+Thread.currentThread().getId());
			CharSequence tabText = tab.getText();
			Log.i(TAG, tabText.toString());
			if(tabText.equals("Profile")) {			
				ListProfileTask task = new ListProfileTask(activity,ft);
				task.execute();
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
		}
    }
}