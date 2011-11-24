package com.android.example.contact.task;

import java.util.LinkedList;
import java.util.List;

import com.android.example.contact.ProfileFragment;
import com.android.example.contact.R;
import com.android.example.contact.data.Pair;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;


public class ListProfileTask extends AsyncTask<Void, Void, List<Pair>> {
	private FragmentTransaction ft;
	private Activity activity;

	public ListProfileTask(Activity act, FragmentTransaction ft) {
		this.activity = act;
		this.ft = ft;
	}

	@SuppressWarnings({ "unused" })
	protected List<Pair> doInBackground(Void... params) {
		Log.i("","AsynchTask Thread Profile"+Thread.currentThread().getId());
		Cursor c = activity.getContentResolver().query(
				ContactsContract.Profile.CONTENT_URI, null, null, null, null);

		int count = c.getCount();

		String[] columnNames = c.getColumnNames();
		List<Pair> profileList = new LinkedList<Pair>();
		boolean b = c.moveToFirst();
		int position = c.getPosition();
		if (count == 1 && position == 0) {
			for (int i = 0; i < count; i++) {
				for (int j = 0; j < columnNames.length; j++) {
					String columnName = columnNames[j];
					Pair pair = new Pair(columnName, c.getString(c
							.getColumnIndex(columnName)));
					profileList.add(pair);
				}
				boolean b2 = c.moveToNext();
			}
		}
		c.close();
		return profileList;
	}

	protected void onPostExecute(List<Pair> result) {
		ProfileFragment profileFragment = (ProfileFragment) activity
				.getFragmentManager().findFragmentByTag("Profile");
		if (profileFragment == null) {
			profileFragment = new ProfileFragment();
		}
		profileFragment.setDataList(result);
		ft.replace(R.id.fragment_container, profileFragment, "Profile");
		ft.commit();
		profileFragment.taskRun = true;
	}
}
