package com.example.android.honeypad;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckedRelativeLayout extends RelativeLayout implements Checkable {

	private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };
	
	private boolean mIsChecked;

	public CheckedRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CheckedRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckedRelativeLayout(Context context) {
		super(context);
	}

	@Override
	public boolean isChecked() {
		return mIsChecked;
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (mIsChecked) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mIsChecked == checked)
			return;
		mIsChecked = checked;
		refreshDrawableState();
	}

	@Override
	public void toggle() {
		setChecked(!mIsChecked);
	}

}
