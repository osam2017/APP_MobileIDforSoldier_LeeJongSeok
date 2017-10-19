package com.example.administrator.mobileidforsoldier;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 출입증을 태그해서 등록할 것인지, 정보를 직접 입력하여 등록할 것인지를 사용자에게 물어보고,
 * 그 응답에 따른 액티비티를 호출하는 액티비티.
 */

public class UserEditActivity extends AppCompatActivity implements OnClickListener {
    // 인텐트 요청 코드 선언
    public static final int REQUSET_CODE_ADD_NFC = 1;
    public static final int REQUEST_CODE_ADD_SELF = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        // 버튼 객체 참조 및 클릭 리스너 설정
        Button buttonNFC = (Button)findViewById(R.id.add_nfc);
        Button buttonSelf = (Button)findViewById(R.id.add_self);
        buttonNFC.setOnClickListener(this);
        buttonSelf.setOnClickListener(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(resultCode) {    // 출입증 등록을 취소한 경우
            case RESULT_CANCELED:
                Toast.makeText(this, "출입증 등록을 취소했습니다.", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case RESULT_OK:     // 출입증을 등록한 경우
                Toast.makeText(this, "출입증이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.add_nfc:      // 출입증을 기기에 태그해서 NFC로 받은 데이터로 ID 카드 정보를 추가하는 액티비티 호출
                Intent intent1 = new Intent(getApplicationContext(), AddwithNFC.class);
                startActivityForResult(intent1, REQUSET_CODE_ADD_NFC);
                break;
            case R.id.add_self:     // 출입증 정보를 직접 입력해서 받은 데이터로 ID 카드 정보를 추가하는 액티비티 호출
                Intent intent2 = new Intent(getApplicationContext(), AddwithSelf.class);
                startActivityForResult(intent2, REQUEST_CODE_ADD_SELF);
                break;
        }
    }
}
