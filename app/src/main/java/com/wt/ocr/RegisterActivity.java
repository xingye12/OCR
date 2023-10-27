package com.wt.ocr;

import static com.wt.ocr.InitActivity.flag;
import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.HttpUtil;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText phoneText;
    private EditText passwordText;
    private EditText addressText;
    private EditText nicknameText;
    private EditText idcardText;
    private RadioButton maleBtn;
    private RadioButton femaleBtn;
    private Button backBtn;
    private Button registerBtn;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_register);
        Intent intentExtra=getIntent();
        ArrayList<User> users=intentExtra.getParcelableArrayListExtra("users");
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是注册界面");
        usernameText=findViewById(R.id.registerUsernameText);
        passwordText=findViewById(R.id.registerPasswordText);
        phoneText=findViewById(R.id.registerPhoneText);
        addressText=findViewById(R.id.registerAddressText);
        nicknameText=findViewById(R.id.registerNickNameText);
        idcardText=findViewById(R.id.registerIdCardText);
        maleBtn=findViewById(R.id.registerMaleBtn);
        femaleBtn=findViewById(R.id.registerFemaleBtn);
        backBtn=findViewById(R.id.registerBackBtn);
        registerBtn=findViewById(R.id.registerYesBtn);
        usernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是账号输入框，请输入账号");
            }
        });
        passwordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是密码输入框，请输入密码");
            }
        });
        phoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是手机号码输入框，请输入手机号码");
            }
        });
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是地址输入框，请输入地址");
            }
        });
        idcardText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是身份证号码输入框，请输入您的身份证号码");
            }
        });
        maleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(maleBtn.isChecked())mKqwSpeechSynthesizer.start("您选择性别为男");
                else mKqwSpeechSynthesizer.start("您选择性别为女");
            }
        });
        femaleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if(femaleBtn.isChecked()) mKqwSpeechSynthesizer.start("您选择性别为女");
               else mKqwSpeechSynthesizer.start("您选择性别为男");
            }
        });
        nicknameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是昵称输入框，请输入您想要取的昵称");
            }
        });
        nicknameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                for(User user:users){
                    if(user.getNickname().equals(nicknameText.getText().toString().trim())){
                        mKqwSpeechSynthesizer.start("昵称重复！请修改昵称");
                        break;
                    }
                }
            }
        });
        usernameText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                for(User user:users){
                    if(user.getUsername().equals(usernameText.getText().toString().trim())){
                        mKqwSpeechSynthesizer.start("账号重复！请修改账号");
                        break;
                    }
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是返回按钮，长按返回登陆界面");
            }
        });
        backBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                return true;
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是注册按钮，长按可注册");
            }
        });
        registerBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String attention="";
                if(TextUtils.isEmpty(usernameText.getText().toString())){
                    attention+="账号不可为空。";
                }
                if(TextUtils.isEmpty(passwordText.getText().toString())){
                    attention+="密码不可为空。";
                }
                if(TextUtils.isEmpty(nicknameText.getText().toString())){
                    attention+="昵称不可为空。";
                }
                if(!maleBtn.isChecked()&&!femaleBtn.isChecked()){
                    attention+="未选择性别。";
                }
                if(TextUtils.isEmpty(idcardText.getText().toString())){
                    attention+="身份证号不可为空。";
                }
                if(TextUtils.isEmpty(phoneText.getText().toString())){
                    attention+="电话不可为空。";
                }
                if(TextUtils.isEmpty(addressText.getText().toString())){
                    attention+="住址不可为空。";
                }

                if(!attention.equals("")){
                    mKqwSpeechSynthesizer.start(attention);
                    return true;
                }
                if(idcardText.getText().toString().trim().length()!=18){
                    mKqwSpeechSynthesizer.start("请输入正确的身份证号");
                    return true;
                }
//                new Thread(registerRun).start();
                new Thread(IMRegisterRun).start();
                return true;
            }
        });
    }
    Runnable registerRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
            userCredentials.setUsername(usernameText.getText().toString().trim());
            userCredentials.setPassword(passwordText.getText().toString().trim());
            userCredentials.setNickname(nicknameText.getText().toString().trim());
            userCredentials.setPhone(phoneText.getText().toString().trim());
            userCredentials.setAddress(addressText.getText().toString().trim());
            userCredentials.setIdCard(idcardText.getText().toString().trim());
            String sex="";
            if(maleBtn.isChecked())sex="男";
            else sex="女";
            userCredentials.setSex(sex);
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/register")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();

                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                Object o = jsonData.getData();
                if(code.equals("1")){
                    Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("message","注册成功");
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("注册失败");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable IMRegisterRun=new Runnable() {
        @Override
        public void run() {
            String requestBody = "accid="+usernameText.getText().toString()+
                    "&name="+nicknameText.getText().toString()+"&token="+passwordText.getText().toString();
            String response= HttpUtil.sendPostRequest(requestBody);
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是注册界面");
    }
}