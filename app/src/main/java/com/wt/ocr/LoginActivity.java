package com.wt.ocr;

import static com.wt.ocr.InitActivity.flag;
import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.wt.ocr.pojo.Boke;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.IM.Preferences;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private EditText usernameText;
    private EditText passwordText;
    private Button loginBtn;
    private Button registerBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intentExtra=getIntent();
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);

        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        if(bundleExtra!=null){
            String message=bundleExtra.getString("message");
            mKqwSpeechSynthesizer.start(message+"您现在在登陆界面");
        }else{
            mKqwSpeechSynthesizer.start("欢迎来到登陆界面！");
        }
        setContentView(R.layout.activity_login);
        usernameText=findViewById(R.id.usernametext);
        passwordText=findViewById(R.id.passwordtext);
        loginBtn=findViewById(R.id.loginbtn);
        registerBtn=findViewById(R.id.regesterBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是注册按钮,长按注册账号");
            }
        });
        registerBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(getAllUserRun).start();
                return true;
            }
        });
        usernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是用户名输入框，请输入用户名");
            }
        });
        passwordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是密码输入框，请输入密码");
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是登录按钮,长按可登录");

            }
        });
        loginBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                doIMLogin(usernameText.getText().toString(),passwordText.getText().toString());
                new Thread(loginRun).start();
                return true;
            }
        });


    }
    Runnable loginRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
            userCredentials.setUsername(usernameText.getText().toString().trim());
            userCredentials.setPassword(passwordText.getText().toString().trim());

            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/login")
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
                    Intent intent=new Intent(LoginActivity.this,InitActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    bundle.putString("nickname",nickname);
                    bundle.putString("sex",sex);
                    bundle.putString("address",address);
                    bundle.putString("idCard",idCard);
                    bundle.putString("phone",phone);
                    intent.putExtra("bundle",bundle);
                    flag=1;
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("登陆失败,账号或密码输入错误");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable getAllUserRun = new Runnable() {
        @Override
        public void run() {

            Boke boke=new Boke();
            boke.setType("a");
            Gson gson = new Gson();
            String json = gson.toJson(boke);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/getAllUser")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();

                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                Object object = jsonData.getData();
                if(code.equals("1")){
                    Object o = jsonData.getData();
                    ArrayList<User> users = new ArrayList<>();
                    if (o instanceof ArrayList) {
                        ArrayList<Object> arrayList = (ArrayList<Object>) o;
                        for (Object item : arrayList) {
                            if (item instanceof LinkedTreeMap) {
                                LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>) item;
                                // 从LinkedTreeMap中提取值
                                String username = (String) linkedTreeMap.get("username");
                                String password = (String) linkedTreeMap.get("password");
                                String nickname = (String) linkedTreeMap.get("nickname");
                                String sex = (String) linkedTreeMap.get("sex");
                                String phone=(String)linkedTreeMap.get("phone");
                                String idcard=(String)linkedTreeMap.get("idCard");
                                String address=(String)linkedTreeMap.get("address");
                                User userresult=new User(username,password,idcard,sex,nickname,address,phone);
                                users.add(userresult);
                            }
                        }
                    }
                    Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                    intent.putParcelableArrayListExtra("users", users);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }else{
                    mKqwSpeechSynthesizer.start("获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    private void doIMLogin(String accid, String token) {
        if (TextUtils.isEmpty(accid)){
//            Toast.makeText(this, R.string.tip_account_is_null, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(token)){
//            Toast.makeText(this, R.string.tip_token_is_null, Toast.LENGTH_SHORT).show();
            return;
        }
        LoginInfo loginInfo = new LoginInfo(accid,token);
        NIMClient.getService(AuthService.class).login(loginInfo).setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo result) {
                //保存accid、token，用于下次自动登录。
                Preferences.saveUserAccount(result.getAccount());
                Preferences.saveUserToken(result.getToken());
                App.hasLogined = true;
//                Toast.makeText(LoginActivity.this, R.string.tip_login_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(int code) {
                //清理登录账号、token缓存，调用login失败后，不允许走自动登录。
                Preferences.saveUserAccount("");
                Preferences.saveUserToken("");
                App.hasLogined = false;
                // TODO: 2022/11/15 登录出错，
                switch (code) {
                    case 302:
                        //返回302表示账号密码错误，即登录时传入的AppKey、accid、token三者不匹配
                        mKqwSpeechSynthesizer.start("账号或密码错误！");
                        Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 408:
                        mKqwSpeechSynthesizer.start("网络状况不佳，请稍后重试");
//                        Toast.makeText(LoginActivity.this, R.string.tip_time_out, Toast.LENGTH_SHORT).show();
                        break;
                    case 415:
                        mKqwSpeechSynthesizer.start("网络状况不佳，请稍后重试");
//                        Toast.makeText(LoginActivity.this, R.string.tip_net_error, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        mKqwSpeechSynthesizer.start("登陆失败");
//                        Toast.makeText(LoginActivity.this, R.string.tip_login_fail, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onException(Throwable exception) {
                //清理登录账号、token缓存，调用login失败后，不允许走自动登录。
                Preferences.saveUserAccount("");
                Preferences.saveUserToken("");
                App.hasLogined = false;
                // TODO: 2022/11/15 登录过程发生异常，被sdk捕获。
//                Toast.makeText(LoginActivity.this, R.string.tip_login_exception+exception.getMessage(), Toast.LENGTH_SHORT).show();
                mKqwSpeechSynthesizer.start("登陆异常");
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是登陆界面");
    }
}