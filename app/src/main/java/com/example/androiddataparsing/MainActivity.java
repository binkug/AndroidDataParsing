package com.example.androiddataparsing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity {
    private TextView disp;
    private Button btn;
    //페이지 번호를 저장할 변수
    int pageno = 1;

    //텍스트 뷰에 출력할 데이터를 저장할 변수
    //ListView에 출력하는 경우라면 ArrayList, ListAdapter를 생성해야 한다.
    String msg = "";

    //데이터를 다운 받을 수 있는 클래스
    class  ThreadEx extends Thread{
        @Override
        public void run() {
            try {
                //다운로드 받을 URL 생성
                URL url = new URL("http://192.168.0.117:9000/user/list?pageno="+pageno);
                //연결 객체를 생성하고 옵션 설정
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(30000);
                con.setUseCaches(false);

                //문자열 읽어오기
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line+"");
                }
                //읽어온 데이터를 msg에 추가
                //msg = msg + sb.toString();

                //읽어온 데이터 파싱하기
                JSONObject object = new JSONObject(sb.toString());
                //list 키 안의 배열을 찾아고기
                JSONArray list = object.getJSONArray("list");
                //배열을 순회
                for(int i=0;i<list.length();i=i+1){
                    //배열에서 i번째 데이터 가져오기
                    JSONObject user = list.getJSONObject(i);
                    String userEmail = user.getString("user_email");
                    String userName = user.getString("user_name");
                    String userPassword = user.getString("user_password");
                    msg = msg + userEmail + ":" + userName+ ":" + userPassword +"\n";
                }

                //핸들러에게 출력 요청
                Message message = new Message();
                message.obj = msg;
                handler.sendMessage(message);
            }catch (Exception e){
                Log.e("다운로드 예외",e.getMessage());
            }
        }
    }
    //다운로드 받은 후 데이터를 재출력 하는 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            String data = (String) msg.obj;
            disp.setText(data);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        disp = (TextView)findViewById(R.id.disp);
        btn = (Button)findViewById(R.id.btn);



        //스레드를 생성해서 데이터를 출력
        ThreadEx th = new ThreadEx();
        th.start();

        //버튼을 클릭 했을 때 다음 페이지 데이터 추가하기
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //페이지 번호 추가
                pageno = pageno +1;
                ThreadEx th = new ThreadEx();
                th.start();
            }
        });
    }
}