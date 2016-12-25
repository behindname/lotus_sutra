package com.buddhist.lotus_sutra_free;

import com.buddhist.lotus_sutra_free.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ContentsActivity extends Activity 
{
	Button[] mBtnArrayChapters = new Button[28];
	Integer[] iBtnIds = {R.id.contents_btn1,R.id.contents_btn2,R.id.contents_btn3,R.id.contents_btn4,R.id.contents_btn5,R.id.contents_btn6,R.id.contents_btn7,
			R.id.contents_btn8,R.id.contents_btn9,R.id.contents_btn10,R.id.contents_btn11,R.id.contents_btn12,R.id.contents_btn13,R.id.contents_btn14,
			R.id.contents_btn15,R.id.contents_btn16,R.id.contents_btn17,R.id.contents_btn18,R.id.contents_btn19,R.id.contents_btn20,R.id.contents_btn21,
			R.id.contents_btn22,R.id.contents_btn23,R.id.contents_btn24,R.id.contents_btn25,R.id.contents_btn26,R.id.contents_btn27,R.id.contents_btn28};
	
	int i = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contents);
	
		for( i=0 ; i<mBtnArrayChapters.length ; i++ )
		{
			mBtnArrayChapters[i] = (Button)findViewById(iBtnIds[i]);			
		}
		
		for( i=0 ; i<mBtnArrayChapters.length ; i++ )
		{
			final int index = i;
			
			mBtnArrayChapters[index].setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					int iChapterIdx = index + 1;
					// TODO Auto-generated method stub
					Intent intentMain = new Intent(ContentsActivity.this, MainActivity.class);
					intentMain.putExtra("ChapterIndex", iChapterIdx);
					startActivity(intentMain);
				}
			});
		}
	}
	
	
}
