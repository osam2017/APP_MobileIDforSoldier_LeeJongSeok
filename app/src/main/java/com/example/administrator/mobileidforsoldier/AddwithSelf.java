package com.example.administrator.mobileidforsoldier;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

/**
 * 출입증 정보를 직접 입력하여 등록
 */

public class AddwithSelf extends AppCompatActivity{
    // database 참조를 위한 객체 선언
    private Dao dbHelper;
    private SQLiteDatabase db;

    // 데이터베이스 컬럼 String 선언
    String number;
    String rank;
    String name;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addwith_self);

        // 데이터베이스 객체 참조
        dbHelper = new Dao(this);
        db = dbHelper.getWritableDatabase();

        // EditText 불러오기
        final EditText userNumber = (EditText) findViewById(R.id.editText_Number);
        final EditText userRank = (EditText) findViewById(R.id.editText_Rank);
        final EditText userName = (EditText) findViewById(R.id.editText_Name);
        final EditText userDate = (EditText) findViewById(R.id.editText_Date);

        // Button 객체 불러오기
        Button button_save = (Button) findViewById(R.id.button_save);
        Button button_close = (Button) findViewById(R.id.button_close);

        button_save.setOnClickListener(new View.OnClickListener() {     // 저장 버튼 선택 시
            @Override
            public void onClick(View v) {
                // EditText 에서 값 추출하기
                number = userNumber.getText().toString();
                rank = userRank.getText().toString();
                name = userName.getText().toString();
                date = userDate.getText().toString();

                // 값이 입력되지 않은 EditText가 있을 때는 값을 저장하지 않음.
                if(Objects.equals(number, "") || Objects.equals(rank, "") || Objects.equals(name, "") || Objects.equals(date, "")){
                    Toast.makeText(getApplicationContext(), "4개 모두 입력하셔야 등록할 수 있습니다...", Toast.LENGTH_LONG).show();
                    return;
                }

                // 데이터 추가 후, 액티비티 종료
                dbHelper.insertRecord(db, number, rank, name, date);
                setResult(RESULT_OK);
                finish();
            }
        });

        button_close.setOnClickListener(new View.OnClickListener() {    // 취소 버튼 선택 시
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
