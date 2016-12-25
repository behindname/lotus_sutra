package com.buddhist.lotus_sutra_free;

import com.buddhist.lotus_sutra_free.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class TitleActivity extends Activity implements OnClickListener 
{
	Button mBtnChapters1, mBtnChapters2;
	Button mBtnIntro; 

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_title);
		
		mBtnChapters1 = (Button)findViewById(R.id.title_btn0);
		mBtnChapters2 = (Button)findViewById(R.id.title_btn1);
		mBtnIntro = (Button)findViewById(R.id.title_btn2);
	
		mBtnChapters1.setOnClickListener(this);
		mBtnChapters2.setOnClickListener(this);
		mBtnIntro.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		case R.id.title_btn0:
		case R.id.title_btn1:
			Intent intentContents = new Intent(TitleActivity.this, ContentsActivity.class);
			startActivity(intentContents);
			break;			
		case R.id.title_btn2:
			Intent intentIntro = new Intent(TitleActivity.this, IntroActivity.class);
			startActivity(intentIntro);
			break;			
		}		
	}
	
}
