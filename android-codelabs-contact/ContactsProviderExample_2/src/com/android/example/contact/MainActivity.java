package com.android.example.contact;

import java.util.List;

import com.android.example.contact.task.ListContactTask;
import com.android.example.contact.task.ListProfileTask;
import com.android.example.contact.data.ContactItem;

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
	private ContactListFragment contactListFragment;
	private Tab contactTab;
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
        
        contactTab = actionBar.newTab().setText("Contacts")
        .setTabListener(tabListener);
        actionBar.addTab(contactTab);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }
    public void addContactListFragment(FragmentTransaction ft, 
            List<ContactItem> result) {
	contactListFragment = (ContactListFragment) getFragmentManager().
	findFragmentByTag("ContactList");
	if(contactListFragment == null){
		contactListFragment = new ContactListFragment();
	}
	ft.replace(R.id.fragment_container, contactListFragment);
	ft.commit();	
	contactListFragment.setDataList(result);
	contactListFragment.taskRun = true;
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
			CharSequence tabText = tab.getText();
			Log.i(TAG, tabText.toString());
			if (tabText.equals("Profile")) {
				ListProfileTask task = new ListProfileTask(activity, ft);
				task.execute();
			} else if (tabText.equals("Contacts")) {
				ListContactTask task = new ListContactTask(activity, ft);
				task.execute();
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
		}
    }
}