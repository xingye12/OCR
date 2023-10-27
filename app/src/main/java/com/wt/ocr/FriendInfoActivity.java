package com.wt.ocr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wt.ocr.utils.KqwSpeechSynthesizer;

public class FriendInfoActivity extends AppCompatActivity {
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String username;
    private String friendName;
    private String sex;
    private String address;
    private String phone;
    private String account;
    private TextView nameText;
    private TextView addressText;
    private TextView phoneText;
    private TextView sexText;
    private TextView accountText;
    private Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        nameText = findViewById(R.id.friendInfoNameText);
        addressText = findViewById(R.id.friendInfoAddressText);
        phoneText = findViewById(R.id.friendInfoPhoneText);
        sexText = findViewById(R.id.friendInfoSexText);
        deleteBtn = findViewById(R.id.friendInfoDeleteBtn);
        Intent intentExtra = getIntent();
        Bundle bundleExtra = intentExtra.getBundleExtra("bundle");
        username = bundleExtra.getString("useranme");
        friendName = bundleExtra.getString("friendName");
        account = bundleExtra.getString("account");
        sex = bundleExtra.getString("sex");
        phone = bundleExtra.getString("phone");
        address = bundleExtra.getString("address");
        nameText.setText("昵称：" + friendName);
        addressText.setText("地址：" + address);
        sexText.setText("性别：" + sex);
        phoneText.setText("手机号码：" + phone);
        accountText.setText("账号：" + account);
        nameText.setOnClickListener(onClickListener);
        sexText.setOnClickListener(onClickListener);
        addressText.setOnClickListener(onClickListener);
        phoneText.setOnClickListener(onClickListener);
        accountText.setOnClickListener(onClickListener);
    }

    private View.OnClickListener
            onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.friendInfoNameText:
                    mKqwSpeechSynthesizer.start(nameText.getText().toString());
                    break;
                case R.id.friendInfoSexText:
                    mKqwSpeechSynthesizer.start(sexText.getText().toString());
                    break;
                case R.id.friendInfoPhoneText:
                    mKqwSpeechSynthesizer.start(phoneText.getText().toString());
                    break;
                case R.id.friendInfoAccountText:
                    mKqwSpeechSynthesizer.start(accountText.getText().toString());
                    break;
                case R.id.friendInfoAddressText:
                    mKqwSpeechSynthesizer.start(addressText.getText().toString());
                    break;
            }
        }
    };

}