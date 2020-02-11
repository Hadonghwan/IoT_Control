package com.example.nslngiot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.mindrot.jbcrypt.BCrypt;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button btn_manager = findViewById(R.id.btn_manager_login);
        Button btn_member = findViewById(R.id.btn_member_login);
        Button btn_signup = findViewById(R.id.btn_signup);

        Log.d("진입testpassword:",BCrypt.hashpw("security915!",BCrypt.gensalt(10)));

        btn_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //관리자 모드 눌렀을 시
                Intent intent = new Intent(getApplicationContext(), LoginManagerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_member.setOnClickListener(new View.OnClickListener() { //랩실 부원 전용 눌렀을 시
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginMemberActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //회원가입 눌렀을 시
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}