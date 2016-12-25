package com.buddhist.lotus_sutra_free;

import com.buddhist.lotus_sutra_free.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class IntroActivity extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_intro);
	}	
}
