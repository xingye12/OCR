package com.wt.ocr;

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
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.IM.Preferences;
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

public class ModifyInfoActivity extends AppCompatActivity {
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private static int ismod=0;
    private String username;
    private Button saveBtn;
    private Button backBtn;
    private TextView nicknameText;
    private RadioButton maleRadio;
    private RadioButton femaleRadio;
    private TextView addressText;
    private TextView phoneText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_modify_info);
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        ArrayList<User> users=intentExtra.getParcelableArrayListExtra("users");
        username=bundleExtra.getString("username");
        String nickname= bundleExtra.getString("nickname");
        String sex=bundleExtra.getString("sex");
        String address=bundleExtra.getString("address");
        String phone=bundleExtra.getString("phone");
        saveBtn=findViewById(R.id.saveBtn);
        nicknameText=findViewById(R.id.nicknameText);
        addressText=findViewById(R.id.addressText);
        maleRadio=findViewById(R.id.maleRadio);
        femaleRadio=findViewById(R.id.femaleRadio);
        phoneText=findViewById(R.id.phoneText);
        backBtn=findViewById(R.id.logoutBtn);
        nicknameText.setText(nickname);
        addressText.setText(address);
        phoneText.setText(phone);
        if("男".equals(sex)){
            maleRadio.setChecked(true);
        }else if("女".equals(sex)){
            femaleRadio.setChecked(true);
        }
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        if(ismod==0) mKqwSpeechSynthesizer.start("您已进入修改个人信息界面");
        else if (ismod==1) mKqwSpeechSynthesizer.start("修改成功！您现在在修改个人信息界面");
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
                    if (user.getNickname().equals(nicknameText.getText().toString().trim())){
                        mKqwSpeechSynthesizer.start("昵称已存在，请修改昵称");
                        break;
                    }
                }
            }
        });
        nicknameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("您的昵称是"+nickname+"，您可以修改昵称");
            }
        });

        femaleRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(femaleRadio.isChecked()) mKqwSpeechSynthesizer.start("您选择性别为女");
                else mKqwSpeechSynthesizer.start("您选择性别为男");
            }

        });
        maleRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(maleRadio.isChecked())mKqwSpeechSynthesizer.start("您选择性别为男");
                else mKqwSpeechSynthesizer.start("您选择性别为女");
            }
        });
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("您的地址是"+address+"，您可以可修改地址");
            }
        });
        phoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("您的手机号码是"+phone+"，您可以修改手机号");
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是退出账号按钮,长按退出账号");
            }
        });
        backBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Preferences.saveUserAccount("");
                Preferences.saveUserToken("");
                App.hasLogined = false;
                NIMClient.getService(AuthService.class).logout();
                Intent intent=new Intent(ModifyInfoActivity.this,InitActivity.class);
                startActivity(intent);
                return true;
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是保存按钮，长按保存更改");
            }
        });
        saveBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(TextUtils.isEmpty(nicknameText.getText().toString().trim())){
                    mKqwSpeechSynthesizer.start("昵称不可为空");
                    return false;
                }
                if(!(maleRadio.isChecked()||femaleRadio.isChecked())){
                    mKqwSpeechSynthesizer.start("请选择性别");
                    return false;
                }
                new Thread(modifyRun).start();
                return true;
            }
        });
    }
    Runnable modifyRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
            userCredentials.setUsername(username);
            userCredentials.setNickname(nicknameText.getText().toString().trim());
            userCredentials.setAddress(addressText.getText().toString().trim());
            userCredentials.setPhone(phoneText.getText().toString().trim());
            if(maleRadio.isChecked()){
                userCredentials.setSex("男");
            }else if(femaleRadio.isChecked()){
                userCredentials.setSex("女");
            }
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/modify")
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
                    Map map=gson.fromJson(o.toString(),Map.class);
                    String username=map.get("username").toString();
                    String nickname=map.get("nickname").toString();
                    String sex=map.get("sex").toString();
                    String address=map.get("address").toString();
                    String idCard=map.get("idCard").toString();
                    String phone= new BigDecimal(map.get("phone").toString()).toString();
                    Intent intent=new Intent(ModifyInfoActivity.this,ModifyInfoActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    bundle.putString("nickname",nickname);
                    bundle.putString("sex",sex);
                    bundle.putString("address",address);
                    bundle.putString("idCard",idCard);
                    bundle.putString("phone",phone);
                    intent.putExtra("bundle",bundle);

                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("信息修改失败！");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是修改个人信息界面");
    }
}