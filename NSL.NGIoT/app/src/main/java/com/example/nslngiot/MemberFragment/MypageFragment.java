package com.example.nslngiot.MemberFragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nslngiot.MainActivity;
import com.example.nslngiot.Network_Utill.VolleyQueueSingleTon;
import com.example.nslngiot.R;
import com.example.nslngiot.Security_Utill.RSA;
import com.example.nslngiot.Security_Utill.SQLFilter;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MypageFragment extends Fragment {
    /*===================MEMBER MY Page=================*/

    private final String pw_regex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{8,}$"; // 비밀번호 정규식
    private String member_name="";
    private String member_id="";
    private String member_corrently_pw="";
    private String member_new_pw="";
    private String member_modify_pw="";

    //sql 검증 결과 & default false
    private boolean member_name_filter = false;
    private boolean member_id_filter = false;
    private boolean member_corrently_pw_filter = false;
    private boolean member_new_pw_filter = false;
    private boolean member_modify_pw_filter = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_member_mypage,container,false);
        return  v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Button btn_memeber_Back = getView().findViewById(R.id.btn_member_mypage_back);
        Button btn_memeber_Modifiy = getView().findViewById(R.id.btn_member_mypage_modify);
        Button btn_memeber_logout = getView().findViewById(R.id.btn_member_mypage_logout);

        EditText name = getView().findViewById(R.id.member_name);
        EditText id = getView().findViewById(R.id.member_id);
        EditText corr_pw = getView().findViewById(R.id.member_corrently_pw);
        EditText new_pw = getView().findViewById(R.id.member_new_pw);
        EditText modify_pw = getView().findViewById(R.id.member_modifypw);

        member_name = name.getText().toString();
        member_id = id.getText().toString();
        member_corrently_pw= corr_pw.getText().toString();
        member_new_pw = new_pw.getText().toString();
        member_modify_pw = modify_pw.getText().toString();

        //////////////////////////////방어 코드////////////////////////////
        //SQL 인젝션 특수문자 공백처리 및 방어
        member_name_filter = SQLFilter.sqlFilter(member_name);
        member_id_filter =  SQLFilter.sqlFilter(member_id);
        member_corrently_pw_filter = SQLFilter.sqlFilter(member_corrently_pw);
        member_new_pw_filter = SQLFilter.sqlFilter(member_new_pw);
        member_modify_pw_filter = SQLFilter.sqlFilter(member_modify_pw);
        //////////////////////////////////////////////////////////////////

        btn_memeber_Modifiy.setOnClickListener(new View.OnClickListener() { // 비밀번호 변경
            @Override
            public void onClick(View view) {
                if ("".equals(member_name) || member_name.length() == 0) { // 현재 이름의 공백 입력 및 널문자 입력 시
                    Toast.makeText(getActivity(), "이름를 입력하세요.", Toast.LENGTH_LONG).show();
                } else if ("".equals(member_id) || member_id.length() == 0) { // 현재 아이디(학번)의 공백 입력 및 널문자 입력 시
                    Toast.makeText(getActivity(), "학번을 입력하세요.", Toast.LENGTH_LONG).show();
                } else if ("".equals(member_corrently_pw) || member_corrently_pw.length() == 0) { // 현재 비밀번호의 공백 입력 및 널문자 입력 시
                    Toast.makeText(getActivity(), "현재 비밀번호을 입력하세요.", Toast.LENGTH_LONG).show();
                } else if ("".equals(member_new_pw) ||  member_new_pw.length() == 0) { // 변경할 비밀번호의 공백 입력 및 널문자 입력 시
                    Toast.makeText(getActivity(), "변경할 비밀번호을 입력하세요.", Toast.LENGTH_LONG).show();
                }else if ("".equals(member_modify_pw) || member_modify_pw.length() == 0) { // 변경을 확인할 비밀번호의 공백 입력 및 널문자 입력 시
                    Toast.makeText(getActivity(), "변경 비밀번호를 한번 더 입력하세요.", Toast.LENGTH_LONG).show();
                } else {
                    // 비밀번호 진행 시 SQL 인젝션 검증 절차 진행
                    //////////////////////////////////////////방어 코드////////////////////////////
                    if (member_name_filter || member_id_filter || member_corrently_pw_filter ) {// SQL패턴 발견 시
                        Toast.makeText(getActivity(), "공격시도가 발견되었습니다.", Toast.LENGTH_LONG).show();
                    } else if(member_new_pw_filter || member_modify_pw_filter){
                        Toast.makeText(getActivity(), "공격시도가 발견되었습니다.", Toast.LENGTH_LONG).show();
                    } else if (member_name.length() >= 20 || member_id.length() >= 20 ||member_corrently_pw.length() >= 255) { // DB 값 오류 방지
                        Toast.makeText(getActivity(), "Name or ID or Password too Long error.", Toast.LENGTH_LONG).show();
                    } else if(member_new_pw.length()>=255 || member_modify_pw.length()>=255){ // DB 값 오류 방지
                        Toast.makeText(getActivity(), "New Password or Modify Password too Long error.", Toast.LENGTH_LONG).show();
                    }else {

                        if(member_new_pw.equals(member_modify_pw)) { // 새로운 비밀번호 비교 검증
                            if(member_new_pw.matches(pw_regex)){ // 비밀번호 정책에 올바른 비밀번호 입력 시

                                // 변경할 비밀번호 클라이언트 단에서 해싱10회 진행
                                member_new_pw = BCrypt.hashpw(member_new_pw,BCrypt.gensalt(10));
                                member_ModifyRequest(); // DB로 개인정보 전송
                            } else// 비밀번호 정책에 위배된 비밀번호 입력 시
                                Toast.makeText(getActivity(), "수정할 비밀번호는 특수문자+숫자+영문자 혼합 8자이상입니다.", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getActivity(), "변경 할 비밀번호를 올바르게 입력해주세요.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        btn_memeber_logout.setOnClickListener(new View.OnClickListener() { // 로그아웃
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                //추후에 쉐어드프리퍼런스 삽입예정
                getActivity().finish();
            }
        });
    }

    //데이터베이스로 넘김
    private void member_ModifyRequest() {
        final StringBuffer url = new StringBuffer("http://210.125.212.191:8888/IoT/Login.jsp");

        // RSA암호화 하여 전송 ID(학번)/ 현재PW(평문)/ 새로운pw(해싱) 3가지
        // rsa로하면 속도가너무느려서 하이브리드 암호가 필요한데, 어디에 대칭키를 숨기지
//                 member_id= RSA.rsaEncryption(member_id,RSA.publicKEY);
//                 member_corrently_pw = RSA.rsaEncryption(member_corrently_pw,RSA.publicKEY);
//                 member_new_pw = RSA.rsaEncryption(member_new_pw,RSA.publicKEY);


        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, String.valueOf(url),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        switch (response.trim()) {
                            case "pwdChangeSuccess":
                                Toast.makeText(getActivity(), "비밀번호가 변경되었습니다..", Toast.LENGTH_SHORT).show();
                                //여기서 강제로그아웃을 할 것이냐 말 것이냐
                                break;
                            case "pwdChangeFailed":
                                Toast.makeText(getActivity(), "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                                break;
                            case "idNotExist":
                                Toast.makeText(getActivity(), "ID(학번)가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                                break;
                            case "error":
                                Toast.makeText(getActivity(), "시스템 오류입니다. 다시시도해주세요.", Toast.LENGTH_SHORT).show();
                                break;
                            default: // 접속 지연 시 확인 사항
                                Toast.makeText(getActivity(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                // 사용자 정보 push 진행
                params.put("id", member_id);
                params.put("pwd", member_new_pw);
                params.put("b_pwd", member_corrently_pw);
                params.put("type", "change");

                return params;
            }
        };

        // 캐시 데이터 가져오지 않음 왜냐면 기존 데이터 가져올 수 있기때문
        // 항상 새로운 데이터를 위해 false
        stringRequest.setShouldCache(false);
        VolleyQueueSingleTon.getInstance(this.getActivity()).addToRequestQueue(stringRequest);
    }
}