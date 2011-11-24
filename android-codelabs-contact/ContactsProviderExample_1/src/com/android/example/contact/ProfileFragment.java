package com.android.example.contact;

import java.util.LinkedList;
import java.util.List;

import com.android.example.contact.ProfileAdapter.ViewHolder;
import com.android.example.contact.data.Pair;
import com.android.example.contact.task.ListProfileTask;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class ProfileFragment extends ListFragment {
	private ProfileAdapter mAdapter;
	private List<Pair> pairList = null;
	private LayoutInflater mInflater;
	public boolean taskRun;
	
	public ProfileFragment() {
		pairList = new LinkedList<Pair>();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);	
		if(!taskRun){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ListProfileTask task= new ListProfileTask(getActivity(),ft);
			task.execute();	
		}
		taskRun = true;
		mAdapter = new ProfileAdapter(getActivity(), R.layout.list_item,R.id.key, pairList);
		mAdapter .setInflater(mInflater);
		mAdapter.setLayout(R.layout.list_item);
		setListAdapter(mAdapter );
		getListView().invalidate();
	}
	
	public void setDataList( List<Pair> result) {
		this.pairList = result;
		Activity act = getActivity();
		taskRun = true;
		if (act != null) {
			mInflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			mAdapter = new ProfileAdapter(getActivity(), R.layout.list_item,R.id.key, result);
			mAdapter.setLayout(R.layout.list_item);
			mAdapter .setInflater(mInflater);
			setListAdapter(mAdapter );
			getListView().invalidate();
		}
	}
}

class ProfileAdapter extends ArrayAdapter<Pair> {
	private static String TAG = ProfileAdapter.class.getName();
	private LayoutInflater inflator = null;
	List<Pair> pairList = null;
	private int layout;

	public ProfileAdapter(Context context, int resource,
			int textViewResourceId, List<Pair> objects) {
		super(context, resource, textViewResourceId, objects);
		this.pairList = objects;
	}
	public void setInflater(LayoutInflater mInflater) {
		this.inflator = mInflater;
	}

	public void setLayout(int layout) {
		this.layout = layout;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		try {
			if (convertView == null) {
				convertView = this.inflator.inflate(layout, null);
				holder = new ViewHolder();
				holder.key = (TextView) convertView.findViewById(R.id.key);
				holder.value = (TextView) convertView.findViewById(R.id.value);
				convertView.setTag(holder);
			}else {
				 holder = (ViewHolder) convertView.getTag();
			}
			Pair pair = (Pair) getItem(position);	
			// Linkify.addLinks(holder.value, Linkify.PHONE_NUMBERS);
			String key = pair.key;
			String value = pair.value;
			holder.key.setText(key);
			holder.value.setText(value);
			
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView key;
		TextView value;
	}
	public Filter getFilter() {
		return null;
	}

	public long getItemId(int position) {
		return 1;
	}

	public int getCount() {
		return pairList.size();
	}

	public Pair getItem(int position) {
		return (Pair) super.getItem(position);
	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}
}
