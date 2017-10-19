package com.example.administrator.mobileidforsoldier;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

/**
 * 출입증을 기기에 태그하여 등록
 */

public class AddwithNFC extends AppCompatActivity {
    // 어댑터 객체 선언
    private NfcAdapter mAdapter;

    // database 참조를 위한 객체 선언
    private Dao dbHelper;
    private SQLiteDatabase db;

    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addwith_nfc);

        // 애니메이션 실행용 객체 선언
        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);

        // 데이터베이스 객체 참조
        dbHelper = new Dao(this);
        db = dbHelper.getWritableDatabase();

        mAdapter = NfcAdapter.getDefaultAdapter(this);  // 어댑터 객체 참조
        if (mAdapter == null) {
            Toast.makeText(getApplicationContext(), "NFC를 먼저 활성화해주세요...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "스캔 대기 중...", Toast.LENGTH_LONG).show();
            rippleBackground.startRippleAnimation();    // NFC가 켜져있다면, 애니메이션 실행
        }

        // 인텐트 객체 생성
        Intent targetIntent = new Intent(this, AddwithNFC.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);

        // 인텐트 필터 객체 생성
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("NFC error", e);
        }

        mFilters = new IntentFilter[]{ndef,};
        mTechLists = new String[][]{new String[]{NfcF.class.getName()}};

        Intent passedIntent = getIntent();
        if (passedIntent != null) {
            String action = passedIntent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                processTag(passedIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    private void processTag(Intent passedIntent) {
        Log.d("process tag", "processTag() called.");

        NdefMessage[] msgs;

        Parcelable[] rawMsgs = passedIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs == null) {
            Log.e("NFC Errror", "NDEF is null.");
        } else {
            msgs = new NdefMessage[rawMsgs.length];
            String[] IdCardInfo = new String[4];    // 출입증 정보를 읽어들어 저장할 String 배열 선언
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                IdCardInfo[i] = msgs[i].toString();
            }

            // 데이터 추가 후, 액티비티 종료
            dbHelper.insertRecord(db, IdCardInfo[0], IdCardInfo[1], IdCardInfo[2], IdCardInfo[3]);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            processTag(intent);
        }
    }
}