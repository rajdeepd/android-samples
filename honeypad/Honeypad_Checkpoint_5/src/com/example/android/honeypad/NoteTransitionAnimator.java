package com.example.android.honeypad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.net.Uri;

import com.example.android.honeypad.NotepadActivity.NoteTransitionAnimatorInterface;

public class NoteTransitionAnimator implements NoteTransitionAnimatorInterface {
	// for Property Animation
	private static final String ROTATION_AXIS_PROP = "rotationY";
	private static final int ROTATION_HALF_DURATION = 250;

	@Override
	public void startAnimation(final NoteEditFragment edit,
			final NotepadActivity na, final Uri noteUri,
			final boolean editNoteAdded) {
		// animate the note transition. We do this in 3 steps:
		// 1. Rotate out the current note
		// 2. Switch the data to the new note
		// 3. Rotate in the new note
		ObjectAnimator anim = ObjectAnimator.ofFloat(edit.getView(),
				ROTATION_AXIS_PROP, 0, 90).setDuration(ROTATION_HALF_DURATION);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				na.fillNote(noteUri, editNoteAdded);
				// rotate in the new note
				ObjectAnimator.ofFloat(edit.getView(), ROTATION_AXIS_PROP, -90,
						0).start();
			}
		});
		anim.start();
	}

}
