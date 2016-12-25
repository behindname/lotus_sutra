package com.buddhist.lotus_sutra_free;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.buddhist.lotus_sutra_free.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnClickListener, OnItemClickListener 
{
	
	EditText m_editSearch;
	Button m_btnSearch;
	Spinner m_spnSearch;
	

	ListView m_listviewSearch;
	ArrayList<String> arrlistChapterVerse = new ArrayList<String>();
	ArrayList<String> arrlistSearched = new ArrayList<String>();
	ArrayList<Integer> arrlistSearchedIdx = new ArrayList<Integer>();
	ArrayAdapter<String> adapterSearch;

	String strSearchKey = null;
	int iCurrentChapterIdx = 1;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
	
		m_editSearch = (EditText)findViewById(R.id.search_edit);
		m_btnSearch = (Button)findViewById(R.id.search_btn);
		m_spnSearch = (Spinner)findViewById(R.id.search_spin);
		
		final String[] SrchMode =
			{
					getString(R.string.dialog_search_part)
				//, getString(R.string.dialog_search_entire)
			};		
		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, SrchMode);
		m_spnSearch.setAdapter(adapterSpinner);
		m_spnSearch.setSelection(0);

		m_listviewSearch = (ListView)findViewById(R.id.search_listviewVerse);
		adapterSearch = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrlistSearched);
		m_listviewSearch.setAdapter(adapterSearch);
		
		m_listviewSearch.setOnItemClickListener(this);
		m_btnSearch.setOnClickListener(this);
		
		
		Intent intentMain = getIntent();
		iCurrentChapterIdx = intentMain.getIntExtra("iChapter", 1);
		strSearchKey = intentMain.getStringExtra("strKey");
		
		
		int iRawFileIDs[] = 
			{
				R.raw.lotus_chapter1, R.raw.lotus_chapter2, R.raw.lotus_chapter3, R.raw.lotus_chapter4, R.raw.lotus_chapter5, R.raw.lotus_chapter6, R.raw.lotus_chapter7,
				R.raw.lotus_chapter8, R.raw.lotus_chapter9, R.raw.lotus_chapter10, R.raw.lotus_chapter11, R.raw.lotus_chapter12, R.raw.lotus_chapter13, R.raw.lotus_chapter14,
				R.raw.lotus_chapter15, R.raw.lotus_chapter16, R.raw.lotus_chapter17, R.raw.lotus_chapter18, R.raw.lotus_chapter19, R.raw.lotus_chapter20, R.raw.lotus_chapter21,
				R.raw.lotus_chapter22, R.raw.lotus_chapter23, R.raw.lotus_chapter24, R.raw.lotus_chapter25, R.raw.lotus_chapter26, R.raw.lotus_chapter27, R.raw.lotus_chapter28
			}; 

		ReadVerseFromFile(iRawFileIDs[iCurrentChapterIdx-1]);

		if(strSearchKey != null && strSearchKey.length() != 0)
		{
			Log.i("SearchActivity OnCreate", "SearchKey received from Main");
			UpdateSearchList(strSearchKey);
			
			m_editSearch.setText(strSearchKey);
			
			m_editSearch.requestFocus();
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(m_editSearch.getWindowToken(), 0);
			m_listviewSearch.requestFocus();
		}
		
	}
	
	public Boolean ReadVerseFromFile(int iRawFileId)
	{
		boolean bResult = false;
		
		arrlistChapterVerse.clear();		
		try
		{
			InputStream inputRaw = getResources().openRawResource(iRawFileId);
			
			BufferedReader bufferRead = null;
			bufferRead = new BufferedReader(new InputStreamReader(inputRaw, "UTF-8"),8192);
			String strline = "";
			String strVerse = "";
			
			int idx = 1;
			while( (strline = bufferRead.readLine()) != null)
			{
				strline = strline.trim();
				if(strline.length() == 0)
				{
					Log.e("while FileReading", "There is no content in line");
					continue;
				}
				
				strline = strline + "\n";
				strVerse = strVerse + strline;
				
				if(strline.endsWith("<CL>\n"))
				{
					strVerse = strVerse.replaceAll("<CL>", "");
					strVerse = String.valueOf(idx) + "  " + strVerse;
					arrlistChapterVerse.add(strVerse);
					strVerse = "";
					idx++;						
				}				
			}

			bufferRead.close();
			inputRaw.close();

			bResult = true;
			
		}
		catch(IOException e)
		{
			Toast.makeText(getApplicationContext(), getString(R.string.file_read_err), Toast.LENGTH_LONG).show();
			bResult = false;
		}
				
		return bResult;
	}


	public boolean UpdateSearchList(String strKey)
	{
		boolean bResult = false;
		strKey = strKey.trim();
		if(strKey == null || strKey.length() == 0)
			return false;
		
		arrlistSearched.clear();
		arrlistSearchedIdx.clear();
		
		int iTotalVerseNum = arrlistChapterVerse.size();
		for( int i=0 ; i < iTotalVerseNum ; i++ )
		{
			String strVerse = arrlistChapterVerse.get(i);
			if(strVerse.contains(strKey))
			{
				//Log.i("SearchActivity", "Key Word "+ strKey +" has been found!!!");
				strVerse = strVerse.replace(strKey, "[" + strKey + "]");
				arrlistSearched.add(strVerse);
				arrlistSearchedIdx.add(i+1);
				bResult = true;
			}			
		}	
		adapterSearch.notifyDataSetChanged();
		m_listviewSearch.setSelection(0);
		
		return bResult;		
	}


	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.search_btn:
			strSearchKey = m_editSearch.getText().toString();
			//if( m_spnSearch.getSelectedItemPosition() == 0 )
			if(UpdateSearchList(strSearchKey))
			{
				m_editSearch.requestFocus();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(m_editSearch.getWindowToken(), 0);
				m_listviewSearch.requestFocus();
			}
			else
			{
				Toast.makeText(getApplicationContext(), getString(R.string.search_msg_no_result), Toast.LENGTH_SHORT).show();
			}			
			break;		
		}	
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		// TODO Auto-generated method stub
		if( parent.getId()==R.id.search_listviewVerse )
		{
			int iVerseIdx = arrlistSearchedIdx.get(position);
			
			Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
			intentMain.putExtra("iVerseIdx", iVerseIdx);
			intentMain.putExtra("strKey", strSearchKey);
			
			setResult(RESULT_OK, intentMain);
			finish();				
		}		
	}
}
