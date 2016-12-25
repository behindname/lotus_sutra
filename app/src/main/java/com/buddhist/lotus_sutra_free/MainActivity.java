package com.buddhist.lotus_sutra_free;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener, android.content.DialogInterface.OnClickListener, 
OnItemLongClickListener, OnItemClickListener, OnUtteranceCompletedListener
{
	//ǰ, ��(�Ǵ� ����), ��
	int m_iCurrentChapterIdx = 1;
	int m_iCurrentPartIdx = 1;
	int m_iCurrentVerseIdx = 1;	

	int m_iTotalPartNum = 1;
	final int MAX_LIST_NUM = 20;
		
	//��� ��ư��
	Button m_btnSearch;
	Button m_btnPrev;
	Button m_btnNext;
	
	TextView m_btnChapter;
	String strCurrentChapter = "";
	
	//�ϴ� play ��ư(Text To Speech)
	ToggleButton m_btnPlay;
	ProgressBar m_barPlay;
	TextToSpeech m_tts;
	boolean m_bPlayOn = false;
	
	//����Ʈ�� 
	ListView m_listviewVerse;
	//������ �����͸� �������� ���� ���� ����. ���⼱ �켱 �� ǰ�� ���� ���θ� �� �����ϵ��� ����.
	ArrayList<String> arrlistEntireVerse = new ArrayList<String>();
	//����Ʈ���� adapter�� �ٷ� ����� array����
	ArrayList<String> arrlistVerse = new ArrayList<String>(); 
	//list������ ������ adapter����
	ArrayAdapter<String> adapter;
	
	//�����ϱ�  Dialog
	View	ShareDialogView;
	AlertDialog ShareDialog;
	ArrayList<Integer> arrlistShareVerseIdx = new ArrayList<Integer>();	//������ ������ ��ȣ���� �����Ѵ�.
	TextView txtviewShare;	//Dialogue�� ��Ÿ�� ������ �۱͵� 
	
	//�˻��� ���� ������
	String strKeyWord = null;
	
	
	public void setCurrentVerse(int iVerse)
	{
		//���� ������ ��������� ���� ProgressBar�� ������Ѿ� �ϹǷ� �� �Լ��� ���� �Ǿ���.
		m_iCurrentVerseIdx = iVerse;		
		float fPercent = (float)m_iCurrentVerseIdx/(float)arrlistEntireVerse.size()*(float)m_barPlay.getMax();
		//�ٸ� UI���� �ǵ帰�ٸ� �� �۾��� ���ÿ� �� �� ������ Thread�� ��� �ϳ� ������ ProgressBar�ϳ��� �ǵ帮�Ƿ� Thread�� �� �ʿ䰡 ����. 
		m_barPlay.setProgress((int)fPercent);		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
	
		//����� ��ư�� �����ϴ� �κ�. 
		m_btnSearch = (Button)findViewById(R.id.main_btnSearch);
		m_btnPrev = (Button)findViewById(R.id.main_btnPrev);
		m_btnNext = (Button)findViewById(R.id.main_btnNext);
		m_btnChapter = (TextView)findViewById(R.id.main_btnChapter);
		
		m_btnSearch.setOnClickListener(this);
		m_btnPrev.setOnClickListener(this);
		m_btnNext.setOnClickListener(this);
		m_btnChapter.setOnClickListener(this);
		
		//�ϴ� ��ư ����
		m_btnPlay = (ToggleButton)findViewById(R.id.main_btnPlay);
		m_barPlay = (ProgressBar)findViewById(R.id.main_barPlay);
		
		m_btnPlay.setOnClickListener(this);
		
		
		//TTS ���� setting�ϱ�!!! - �ѱ��� ������� �Ͽ� ������ �켱 �����Ѵ�. 
		m_tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() 
		{			
			@Override
			public void onInit(int status) 
			{
				// TODO Auto-generated method stub
				if(status == TextToSpeech.SUCCESS)
				{
					int iResult = m_tts.setLanguage(Locale.KOREA);
					if(iResult == TextToSpeech.LANG_MISSING_DATA || iResult == TextToSpeech.LANG_NOT_SUPPORTED)
						Toast.makeText(getApplicationContext(), R.string.tts_init_err, Toast.LENGTH_LONG).show();
				}
				else
					Log.e("MainActivity OnCreate", "TTS Initialization failed!");
			}
		} );
		m_tts.setOnUtteranceCompletedListener(this);	//�� ���� �бⰡ �� ������ �� �� �������� �Ѿ�� ���� �����ϴ� �Լ�.
		
		
		
		//���� ListView ��Ʈ
		m_listviewVerse = (ListView)findViewById(R.id.main_listviewVerse);
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_activated_1, arrlistVerse);
		m_listviewVerse.setAdapter(adapter);
		m_listviewVerse.setOnItemLongClickListener(this);
		m_listviewVerse.setOnItemClickListener(this);
		
		//���� �����ϱ� ���� dynamic ���̾�α� �ڽ� ����� 
		//inflater�� ���� View���ٰ� �ش� xml�� �ٿ���,
		LayoutInflater inflator = LayoutInflater.from(this);
		ShareDialogView = inflator.inflate(R.layout.dialogue_share, null);

		//�� View�� �ٽ� Dlg�� ���δ�.
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle(getString(R.string.dialog_verse_share_msg));
		dlg.setView(ShareDialogView);
		dlg.setPositiveButton(getString(R.string.ok_button), this);
		dlg.setNegativeButton(getString(R.string.cancel_button), this);
		ShareDialog = dlg.create();
		
		txtviewShare = (TextView)ShareDialogView.findViewById(R.id.share_txt);
		Button btnDlgPlus = (Button)ShareDialogView.findViewById(R.id.share_btnPlus);
		Button btnDlgMinus = (Button)ShareDialogView.findViewById(R.id.share_btnMinus);
		btnDlgPlus.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				//������ ������Ű�� �� ������Ű�� ���ϸ� '�� �̻� ������ �����ϴ�'�޽����� ����.
				if(!UpdateShareVerses(true))
					Toast.makeText(getApplicationContext(), getString(R.string.dialog_verse_share_limit), Toast.LENGTH_SHORT).show();				
			}
		});
		btnDlgMinus.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				//������ ������Ű�� �� ������Ű�� ���ϸ� '�� �̻� ������ �����ϴ�'�޽����� ����.
				if(!UpdateShareVerses(false))
					Toast.makeText(getApplicationContext(), getString(R.string.dialog_verse_share_limit), Toast.LENGTH_SHORT).show();				
			}
		});
		
		
		String strChapters[] = 
			{
				getString(R.string.content_button1), getString(R.string.content_button2), getString(R.string.content_button3), getString(R.string.content_button4),getString(R.string.content_button5), getString(R.string.content_button6), getString(R.string.content_button7), 
				getString(R.string.content_button8), getString(R.string.content_button9), getString(R.string.content_button10), getString(R.string.content_button11),getString(R.string.content_button12), getString(R.string.content_button13), getString(R.string.content_button14), 
				getString(R.string.content_button15), getString(R.string.content_button16), getString(R.string.content_button17), getString(R.string.content_button18),getString(R.string.content_button19), getString(R.string.content_button20), getString(R.string.content_button21), 
				getString(R.string.content_button22), getString(R.string.content_button23), getString(R.string.content_button24), getString(R.string.content_button25),getString(R.string.content_button26), getString(R.string.content_button27), getString(R.string.content_button28) 
			};

		//ó���� ���ʿ��� Chapter index �޾ƿͼ� ǥ���ϴ� �ڵ�.
		m_iCurrentChapterIdx = getIntent().getIntExtra("ChapterIndex", 1);
		strCurrentChapter = String.valueOf(m_iCurrentChapterIdx)+ " " + strChapters[m_iCurrentChapterIdx-1];
		m_btnChapter.setText(strCurrentChapter);	

		//�ش��ϴ� ǰ�� ���Ͽ��� �������� �켱 ��°�� ���� �� �о�´�.
		int iRawFileIDs[] = 
			{
				R.raw.lotus_chapter1, R.raw.lotus_chapter2, R.raw.lotus_chapter3, R.raw.lotus_chapter4, R.raw.lotus_chapter5, R.raw.lotus_chapter6, R.raw.lotus_chapter7,
				R.raw.lotus_chapter8, R.raw.lotus_chapter9, R.raw.lotus_chapter10, R.raw.lotus_chapter11, R.raw.lotus_chapter12, R.raw.lotus_chapter13, R.raw.lotus_chapter14,
				R.raw.lotus_chapter15, R.raw.lotus_chapter16, R.raw.lotus_chapter17, R.raw.lotus_chapter18, R.raw.lotus_chapter19, R.raw.lotus_chapter20, R.raw.lotus_chapter21,
				R.raw.lotus_chapter22, R.raw.lotus_chapter23, R.raw.lotus_chapter24, R.raw.lotus_chapter25, R.raw.lotus_chapter26, R.raw.lotus_chapter27, R.raw.lotus_chapter28
			}; 
		
		if(ReadVerseFromFile(iRawFileIDs[m_iCurrentChapterIdx-1]))
		{
			//�� ǰ�� ��� ������ �� �о������ MAX_LIST_NUM���� ������ �� ��Ʈ�� �� �� �� ���� ��Ʈ�� �����ϴ��� �˾Ƴ���.
			int iTotalVerseCount = arrlistEntireVerse.size();
			double dCnt = ((double)iTotalVerseCount/(double)MAX_LIST_NUM);
			m_iTotalPartNum = (dCnt%1 == 0.0f)? (int)dCnt:(int)dCnt+1;
			
			//���� ��Ʈ�� ������ �����ֱ�
			UpdateVerseList(m_iCurrentPartIdx);
		}
		
	}
	
	public Boolean ReadVerseFromFile(int iRawFileId)
	{
		boolean bResult = false;
		
		arrlistEntireVerse.clear();		
		try
		{
			//��ȭ�� �� ǰ�� ��°�� ������ txt������ raw�����κ��� �д´�.
			InputStream inputRaw = getResources().openRawResource(iRawFileId);
			
			BufferedReader bufferRead = null;
			bufferRead = new BufferedReader(new InputStreamReader(inputRaw, "UTF-8"),8192);
			String strline = "";
			String strVerse = "";
			
			while( (strline = bufferRead.readLine()) != null)
			{
				strline = strline.trim();
				if(strline.length() == 0)
				{
					Log.e("while FileReading", "No content in line");
					continue;
				}
				
				strline = strline + "\n";
				strVerse = strVerse + strline;
				
				if(strline.endsWith("<CL>\n"))
				{
					strVerse = strVerse.replaceAll("<CL>", "");					
					arrlistEntireVerse.add(strVerse);
					strVerse = "";
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

	//���ڷ� ���� part�� verse��� ListView�� update�Ѵ�.
	public boolean UpdateVerseList(int iPartIdx)
	{
		if( iPartIdx < 1 || iPartIdx > m_iTotalPartNum )
			return false;
		
		m_iCurrentPartIdx = iPartIdx;
		
		arrlistVerse.clear();
		
		int iBegin = (m_iCurrentPartIdx-1)*MAX_LIST_NUM;
		int iEnd = iBegin + MAX_LIST_NUM;
		setCurrentVerse( iBegin + 1 );
		
		String strVerseWithIdx = "";
		for( int i = iBegin ; i < iEnd ; i++ )
		{
			if(i >= arrlistEntireVerse.size())
				break;
			
			strVerseWithIdx = arrlistEntireVerse.get(i);
			strVerseWithIdx = String.valueOf(i+1) + "\t" + strVerseWithIdx;
			arrlistVerse.add(strVerseWithIdx);
		}				
		
		adapter.notifyDataSetChanged();
		//üũ����ٰ� �������־�� single choice�̹Ƿ� �ٸ� ���� ���õ� �� �����ȴ�. 
		m_listviewVerse.setItemChecked(0, true);
		m_listviewVerse.setItemChecked(0, false);
		m_listviewVerse.setSelection(0);
		
		Log.i("UpdateVerseList", "Selection initialized.");
					
		String strCurrentPart = "(" + String.valueOf(m_iCurrentPartIdx) +"/" + String.valueOf(m_iTotalPartNum)+")";
		m_btnChapter.setText(strCurrentChapter + strCurrentPart);
		
		return true;		
	}

	//������ ���缭 ListView�� update�ϴ� �Լ�. 
	public boolean FollowVerseList(int iVerseIdx)
	{
		int iVersePosition = 0;
		if(iVerseIdx > arrlistEntireVerse.size())
			return false;
		
		double dPart = ((double)iVerseIdx/(double)MAX_LIST_NUM);
		int iPartIdx = (dPart%1 == 0.0f)? (int)dPart:(int)dPart+1;
		//��Ʈ�� ���ߴٸ� ListView�� �������� ������ ���� �ٲ��. �� �� ���� ����index�� �� ��Ʈ�� �� ó�� ������ �ȴ�. 
		if( m_iCurrentPartIdx != iPartIdx )
		{			
			UpdateVerseList(iPartIdx);
		}
		
		//��Ʈ�� �ٲ� ���� �ƴ϶�� �ܼ��� �� �������� üũ�ϸ� �ȴ�. 
		setCurrentVerse( iVerseIdx );									
		iVersePosition = m_iCurrentVerseIdx - (m_iCurrentPartIdx-1)*MAX_LIST_NUM - 1;		
		m_listviewVerse.setItemChecked(iVersePosition, true);
		//���� ��� �߿� �ʹ� ���� �����ϸ� ��������� 5 �������� �� ���� �����Ѵ�.(5�� ����� �ƴϸ� �������� �ʴ´�.)  
		if(!(m_bPlayOn && (iVersePosition % 5 != 0)))
			m_listviewVerse.setSelection(iVersePosition);
		
				
		return true;
	}

	@Override
	public void onClick(View v) 
	{
		int iButtonID = v.getId();
		
		//���� tts�� �÷��� ���̸� 
		if(m_bPlayOn)
		{
			//�÷��� ��ư, ���� ��ư �ܿ��� �� �������� ��ġ�Ѵ�. 
			if(iButtonID != R.id.main_btnPlay && iButtonID != R.id.main_btnChapter)
			{
				Toast.makeText(getApplicationContext(), getString(R.string.tts_play_msg), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
		switch(iButtonID)
		{
		case R.id.main_btnPrev:			
			if(!UpdateVerseList(m_iCurrentPartIdx - 1))
				Toast.makeText(getApplicationContext(), getString(R.string.msg_part_begin), Toast.LENGTH_SHORT).show();
			
			break;		
		case R.id.main_btnNext:
			if(!UpdateVerseList(m_iCurrentPartIdx + 1))
				Toast.makeText(getApplicationContext(), getString(R.string.msg_part_end), Toast.LENGTH_SHORT).show();
				
			break;			
		case R.id.main_btnChapter:
			finish();
			break;
			
		case R.id.main_btnPlay:
			//tts�������� ���� ����ϴ� �κ�!
			m_bPlayOn = m_btnPlay.isChecked();
			PlayTTS(m_bPlayOn, m_iCurrentVerseIdx);
			if(m_bPlayOn)	//���� �÷��̽ÿ� ���� ���� highlight���ش�. �� �������ʹ� FollowVerse���� ó���Ѵ�...
			{
				int iVersePosition = m_iCurrentVerseIdx - (m_iCurrentPartIdx-1)*MAX_LIST_NUM - 1;		
				m_listviewVerse.setItemChecked(iVersePosition, true);
				m_listviewVerse.setSelection(iVersePosition);
			}
			break;
			
		case R.id.main_btnSearch:
			Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
			intentSearch.putExtra("iChapter", m_iCurrentChapterIdx);
			intentSearch.putExtra("strKey", strKeyWord);
			
			startActivityForResult(intentSearch,0);
			break;	
		}
	}
	
	public void PlayTTS(boolean bPlay, int iVerseIdx)
	{
		//���� ���¶�� tts������ ������Ų��.
		if(!bPlay)
		{
			m_tts.stop();			
		}
		else
		{			
			HashMap<String, String> ttsMsg = new HashMap<String, String>();
			ttsMsg.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "VerseEnd");
			
			String strReadingVerse = arrlistEntireVerse.get(iVerseIdx-1);
			strReadingVerse = strReadingVerse.replaceAll("[\u4e00-\u9fff]", "");	//���� ����
			strReadingVerse = strReadingVerse.replaceAll("['('')''['']'.]", "");	//Ư�� ��ȣ ����
			m_tts.speak(strReadingVerse, TextToSpeech.QUEUE_FLUSH, ttsMsg);			
		}		
	}
	
	//tts���� ���ϱⰡ ������ �� �Ҹ��� �Լ�.
	@Override
	public void onUtteranceCompleted(final String utteranceId) 
	{
		//ȭ���� �ٸ� ����-ListView-�� �ǵ帮�� ���ؼ��� uiThread�� ��� �Ѵ�.
		runOnUiThread(new Runnable()
		{
			@Override
			public void run() 
			{
				if(m_bPlayOn && utteranceId.equals("VerseEnd"))
				{
					if(m_iCurrentVerseIdx < arrlistEntireVerse.size())
					{
						m_iCurrentVerseIdx++;			
						FollowVerseList(m_iCurrentVerseIdx);	//�� �ȿ��� ListView�� �ǵ帮��, (UIThread)
						PlayTTS(m_bPlayOn, m_iCurrentVerseIdx);	//���⼭ �� ���������� TTS�� ���µ� �� �۾��� ���ÿ� �Ƿ��� Thread�� ��� �Ѵ�.			
					}
					else						//���� ��� ���� ������ ������ �����̶�� �� �̻� ���� �ʴ´�.
					{
						m_tts.stop();
						m_bPlayOn = false;
						m_btnPlay.setChecked(false);
						Toast.makeText(getApplicationContext(), getString(R.string.msg_part_end), Toast.LENGTH_SHORT).show();				
					}
				}				
			}
		});		
	}

	//�˻��ؼ� ã�� ������ ���� �ڵ�!!!
	//SearchActivity���� (PartIdx), VerseIdx, KeyWord�� �޾ƿͼ� �ٽ� MainActivity�� Renew�ؾ� �Ѵ�.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		// TODO Auto-generated method stub
		if( resultCode == RESULT_OK )
		{			
			int iCurrentVerseIdx = data.getIntExtra("iVerseIdx", 1);
			strKeyWord = data.getStringExtra("strKey");			
			FollowVerseList(iCurrentVerseIdx);
		}		
	}	
	public boolean UpdateShareVerses(boolean bIncrease)
	{
		int iSize = arrlistShareVerseIdx.size();
		String strShare = "";

		if(bIncrease)			//���� �ϳ� �� �߰�
		{
			int iVerseLastIdx = arrlistShareVerseIdx.get(iSize-1);			
			if(iVerseLastIdx+1 >= arrlistEntireVerse.size())				//������ �ϳ� �� �߰��ϴµ� �� ������ ���� �����̸� false�� return�Ѵ�.
				return false;
			
			arrlistShareVerseIdx.add(iVerseLastIdx+1);			
		}
		else	//���� �ϳ� ����
		{
			if(iSize <= 1)				//������ �ϳ� �� ���µ� �� �� ������ ���ٸ� false�� return�Ѵ�.
				return false;
			
			arrlistShareVerseIdx.remove(iSize-1);			
		}
		
		iSize = arrlistShareVerseIdx.size();
		for(int i = 0; i < iSize ; i++)
		{
			int idx = arrlistShareVerseIdx.get(i);
			strShare += arrlistEntireVerse.get(idx) + "\n";
		}
		txtviewShare.setText(strShare);				
		return true;		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		if(parent.getId() == R.id.main_listviewVerse)
		{
			if(m_bPlayOn)
			{
				m_listviewVerse.setItemChecked(position, false);
				int iCurrentPosition = m_iCurrentVerseIdx - (m_iCurrentPartIdx-1)*MAX_LIST_NUM - 1;		
				m_listviewVerse.setItemChecked(iCurrentPosition, true);
				Toast.makeText(getApplicationContext(), getString(R.string.tts_play_msg), Toast.LENGTH_SHORT).show();
			}
			else
			{
				setCurrentVerse( (m_iCurrentPartIdx-1)*MAX_LIST_NUM + position + 1);
			}
		}
	}
	
	//ListView���� ������ �� ������ �� �ش� ���� �����ϰ� ���� ��ȭ ���� �߿��
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
	{
		// TODO Auto-generated method stub
		if(parent.getId() == R.id.main_listviewVerse)
		{
			if(m_bPlayOn)
			{
				Toast.makeText(getApplicationContext(), getString(R.string.tts_play_msg), Toast.LENGTH_SHORT).show();
				return false;
			}
			
			//�ϴ� ���� ǰ�� ������ �׳� �� ���� ������ �״�� �����ؿ���.
			TextView txtviewChapter = (TextView)ShareDialogView.findViewById(R.id.share_chapter);
			txtviewChapter.setText(strCurrentChapter);
			
			//���� ������ ������ idx��ȣ�� �켱 �����ϰ�,
			arrlistShareVerseIdx.clear();
			int iVerseIdx = (m_iCurrentPartIdx-1)*MAX_LIST_NUM + position;
			arrlistShareVerseIdx.add(iVerseIdx);
			
			//�� idx��ȣ�� �ش��ϴ� �۱͸� �ϴ� ����ִ´�.
			String strShareVerse = arrlistEntireVerse.get(iVerseIdx);
			txtviewShare.setText(strShareVerse);
			
			ShareDialog.show();			
		}
		return false;
	}	

	
	//�����ϱ� ��ȭ���� OK��ư ������ �� ������ ���� �����ϴ� ��� ����!
	@Override
	public void onClick(DialogInterface dialog, int which) 
	{
		// TODO Auto-generated method stub
		if(dialog.equals(ShareDialog))
		{
			if(which == DialogInterface.BUTTON_POSITIVE)
			{
				Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
				intentShare.setType("text/plain");
				
				String strTitle = getString(R.string.sutra_name) + " " + strCurrentChapter;
				String strShareVerses = txtviewShare.getText().toString();
				intentShare.putExtra(Intent.EXTRA_SUBJECT, strTitle);
				intentShare.putExtra(Intent.EXTRA_TEXT, strShareVerses);
				
				startActivity(Intent.createChooser(intentShare, getString(R.string.dialog_verse_share)));
			}			
		}		
	}
	

	@Override
	protected void onDestroy() 
	{
		m_tts.stop();
		m_tts.shutdown();
		m_tts = null;
		super.onDestroy();
	}	
	
	
}
