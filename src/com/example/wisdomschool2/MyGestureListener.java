package com.example.wisdomschool2;


import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

public class MyGestureListener implements OnGestureListener {
	private ViewFlipper flipper;
	public MyGestureListener(ViewFlipper flipper) {
		this.flipper=flipper;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(e1.getX()-e2.getX()>120){
			flipper.setInAnimation(flipper.getContext(), R.anim.in_rightleft);
			flipper.setOutAnimation(flipper.getContext(), R.anim.out_rightleft);
			flipper.showNext();
			return true;
		}
		if(e2.getX()-e1.getX()>120){
			flipper.setInAnimation(flipper.getContext(), R.anim.in_leftright);
			flipper.setOutAnimation(flipper.getContext(), R.anim.out_leftright);
			flipper.showPrevious();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}



	

}
