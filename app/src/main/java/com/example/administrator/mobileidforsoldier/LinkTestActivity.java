package com.example.administrator.mobileidforsoldier;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * 기기에 저장된 출입증이 서버에 등록되어 있는 정식 출입증이 맞는지를 리더기 없이 직접 서버와 통신해서 확인할 수 있는 액티비티.
 */
public class LinkTestActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private Dao dbHelper;
    private SQLiteDatabase db;

    // 출입증 데이터 column 의 총 갯수
    public static final int TOTAL_DATA_COLUMN_NUMBER = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_test);

        // 객체를 불러온 후, 리스너 설정
        Button button = (Button)findViewById(R.id.button);
        EditText editText = (EditText) findViewById(R.id.editText);
        button.setOnClickListener(this);
        editText.setOnKeyListener(this);
    }

    @Override
    public void onClick(View v) {       // 서버 송신 버튼이 눌렸을 때
        switch(v.getId()) {
            case R.id.button:
                sendReq2Server();
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {     // 엔터키가 눌렸을 때
        if(keyCode == event.KEYCODE_ENTER || keyCode == event.KEYCODE_NUMPAD_ENTER) {
            sendReq2Server();
            return true;
        }
        return false;
    }

    public void sendReq2Server() {      // 서버로 요청하는 메소드
        final TextView textView = (TextView)findViewById(R.id.textView_result);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final Button button = (Button) findViewById(R.id.button);

        // 요청을 받기 전에는 추가 요청을 하지 못하도록 하기 위해서 버튼과 텍스트 입력칸을 비활성화
        button.setClickable(false);
        editText.setEnabled(false);

        String url = "http://" + editText.getText().toString() + ":5037/confirmcard";
        Log.i("Request URL", url);

        // 서버에 데이터를 요청한 후, 문자열 타입으로 된 응답을 받아오는 Volley 모듈의 StringRequest 객체 생성
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("Volley Response", "onResponse 호출됨.");
                            // 서버에서 받은 response 문자열을 TextView와 토스트에 띄운다.
                            textView.setText(response);
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("onResponse Error", "응답 처리 중 에러 발생", e);
                        }
                        // 버튼과 입력칸을 다시 활성화
                        button.setClickable(true);
                        editText.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {        // 서버에서 제대로 된 응답을 받지 못했을 경우
                Log.e("Volley ErrorResponse", "onErrorResponse 호출됨.", error);
                textView.setText("ERROR");

                // 버튼과 입력칸을 다시 활성화
                button.setClickable(true);
                editText.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() {     // 서버로 보낼 해시맵 데이터를 설정하는 메소드
                Map<String, String> params = new HashMap<>();

                // 데이터베이스 객체 참조
                dbHelper = new Dao(getApplicationContext());
                db = dbHelper.getWritableDatabase();

                // 출입증 데이터 검색
                Cursor c = null;
                int recordCount = 0;

                try {
                    c = db.rawQuery("SELECT servicenumber, rank, name, date FROM " + Dao.getTableName() + ";", null);
                    recordCount = c.getCount();
                } catch (Exception e){
                    Log.e("Query Error", "LinkTestActivity에서 출입증 정보 검색 중 에러 발생", e);
                }

                String[] IdCardInfo = new String[TOTAL_DATA_COLUMN_NUMBER];    // 출입증 정보를 데이터베이스에서 불러와 저장할 String 배열 선언

                if (recordCount > 0) {    // 출입증 데이터가 존재한다면
                    c.moveToFirst();
                    for (int i = 0; i < TOTAL_DATA_COLUMN_NUMBER; i++) {
                        IdCardInfo[i] = c.getString(i);
                    }

                    c.close();
                }

                // 서버에 보낼 정보 지정
                params.put("ServiceNumber", IdCardInfo[0]);
                params.put("Rank", IdCardInfo[1]);
                params.put("Name", IdCardInfo[2]);
                params.put("Date", IdCardInfo[3]);

                return params;
            }
        };

        request.setShouldCache(false);
        Volley.newRequestQueue(getApplicationContext()).add(request);
        Toast.makeText(getApplicationContext(), "요청함...", Toast.LENGTH_SHORT).show();
    }
}
