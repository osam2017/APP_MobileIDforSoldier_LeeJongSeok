package com.example.administrator.mobileidforsoldier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.core.deps.guava.primitives.Bytes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    // 인텐트 요청 코드 선언
    public static final int REQUEST_CODE_USER_EDIT = 100;
    public static final int REQUEST_CODE_LINK_TEST = 200;
    public static final int REQUEST_CODE_GALLERY_CODE = 300;

    // 출입증 데이터 column 의 총 갯수
    public static final int TOTAL_DATA_COLUMN_NUMBER = 4;

    // NFC 사용을 위한 어댑터 객체와 Ndef메시지 객체 선언
    private NfcAdapter mAdapter = null;
    private NdefMessage nMessage = null;

    // database 참조를 위한 SQLiteDatabase 객체 선언
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = NfcAdapter.getDefaultAdapter(this);  // 어댑터 객체 참조

        if(mAdapter == null) {      // NFC 기능이 비활성화 되어 있는 경우
            Toast.makeText(getApplicationContext(), "NFC를 활성화해주세요...", Toast.LENGTH_LONG).show();
        }

        // 버튼 객체 참조 및 클릭 리스너 설정
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(this);
        Button button = (Button)findViewById(R.id.button_LinkTest);
        FloatingActionButton imgbutton = (FloatingActionButton)findViewById(R.id.button_addphoto);
        button.setOnClickListener(this);
        imgbutton.setOnClickListener(this);


        // 데이터베이스 열기
        boolean isOpen = openDatabase();
        if(isOpen) {
            executeRawQuery();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAdapter != null) {
            mAdapter.setNdefPushMessage(nMessage, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(resultCode) {
            case RESULT_OK:
            switch(requestCode) {
                case REQUEST_CODE_USER_EDIT:
                case REQUEST_CODE_LINK_TEST:
                    openDatabase();
                    executeRawQuery();
                    break;
                case REQUEST_CODE_GALLERY_CODE:
                    SetPicture(intent); // 갤러리에서 가져오기
                    break;
            }
        }
    }

    private boolean openDatabase() {    // 데이터베이스 객체 참조
        Dao dbHelper = new Dao(this);
        db = dbHelper.getWritableDatabase();

        return true;
    }

    private void executeRawQuery() {
        // 출입증 데이터 검색
        Cursor c = null;
        int recordCount = 0;

        try {
            c = db.rawQuery("SELECT servicenumber, rank, name, date FROM " + Dao.getTableName() + ";", null);
            recordCount = c.getCount();
        } catch (Exception e) {
            Log.e("Query Error", "MainActivity에서 출입증 데이터 조회 중 에러 발생", e);
        }

        if(recordCount> 0) {    // 출입증 데이터가 존재한다면
            c.moveToFirst();
            String[] IdCardInfo = new String[TOTAL_DATA_COLUMN_NUMBER];    // 출입증 정보를 데이터베이스에서 불러와 저장할 String 배열 선언

            for(int i = 0; i < TOTAL_DATA_COLUMN_NUMBER; i++){
                IdCardInfo[i] = c.getString(i);
            }

            // 출입증 정보를 화면에 띄우기
            TextView IdInfoView = (TextView) findViewById(R.id.id_textView);
            TextView howUseView = (TextView) findViewById(R.id.textView_howUse);
            IdInfoView.setText("[출입증 정보]\n" + "군번: " + IdCardInfo[0] + "\n계급: " + IdCardInfo[1] + "\n이름: " + IdCardInfo[2] + "\n발급일: " + IdCardInfo[3]);
            howUseView.setText("- 기기를 리더기에 태그하시면 인증됩니다...\n" +
                                "- 출입증을 변경하시려면 중앙의 카드 버튼을 누르세요.\n" +
                                "- 기기에 있는 본인 사진을 등록하시려면 우측 하단 버튼을 누르세요.");

            // 애니메이션 실행
            final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content_main);
            rippleBackground.startRippleAnimation();

            // 사진 추가 버튼 활성화
            final TextView photoBtnText = (TextView)findViewById(R.id.photoBtn_text);
            final FloatingActionButton addPhotoButton = (FloatingActionButton) findViewById(R.id.button_addphoto);
            photoBtnText.setVisibility(View.VISIBLE);
            addPhotoButton.setVisibility(View.VISIBLE);
            addPhotoButton.setClickable(true);

            if(mAdapter != null) {
                nMessage = createTagMessage(IdCardInfo);     // NFC 메시지 객체 생성
            }

            c.close();
        }
    }

    private NdefMessage createTagMessage(String msg[]) {
        NdefRecord[] records = new NdefRecord[TOTAL_DATA_COLUMN_NUMBER];

        for(int i = 0; i < TOTAL_DATA_COLUMN_NUMBER; i++) {
            records[i] = createTextRecord(msg[i], Locale.KOREAN, true);    // 텍스트 레코드 객체 배열 생성
        }
        return new NdefMessage(records);    // 메시지 객체 생성
    }

    // NFC 발신용 텍스트 레코드 객체를 만들어서 리턴
    private NdefRecord createTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        final byte[] langBytes = locale.getLanguage().getBytes(StandardCharsets.US_ASCII);
        final Charset utfEncoding = encodeInUtf8 ? StandardCharsets.UTF_8 : Charset.forName("UTF-16");
        final byte[] textBytes = text.getBytes(utfEncoding);
        final int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        final char status = (char) (utfBit + langBytes.length);
        final byte[] data = Bytes.concat(new byte[] {(byte) status}, langBytes, textBytes);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton:      // 출입증 등록 버튼 클릭 시
                Intent intent1 = new Intent(getApplicationContext(), UserEditActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_USER_EDIT);
                break;
            case R.id.button_LinkTest:      // 기기에 등록된 출입증이 서버에 있는 출입증과 일치하는지 수동으로 테스트 하기 위한 액티비티 호출
                Intent intent2 = new Intent(getApplicationContext(), LinkTestActivity.class);
                startActivityForResult(intent2, REQUEST_CODE_LINK_TEST);
                break;
            case R.id.button_addphoto:      // 사진 추가 버튼 클릭 시
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  // 기기 저장소(갤러리) 접근 권한이 부여되었는지를 확인
                        == PackageManager.PERMISSION_GRANTED) {
                    SellectPhoto();
                } else {    // 권한이 부여되지 않았다면, 권한을 요청
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SellectPhoto();
                } else {
                    Toast.makeText(this, "갤러리 접근 권한 요청 거부됨.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void SellectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY_CODE);
    }

    private void SetPicture(Intent data) {
        Uri imgUri = data.getData();
        String imagePath = getRealPathFromURI(imgUri); // 이미지가 저장되어 있는 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert exif != null;
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);    // 경로를 통해 비트맵으로 전환
        ImageButton imageButton = (ImageButton)findViewById(R.id.imageButton);
        imageButton.setImageBitmap(rotate(bitmap, exifDegree));  // 이미지 뷰에 비트맵 넣기
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private Bitmap rotate(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 세팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 세팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }
}