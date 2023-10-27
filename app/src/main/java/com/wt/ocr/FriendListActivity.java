package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
//import com.netease.yunxin.kit.corekit.im.model.UserInfo;
//import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
//import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.CustomAdapter;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.OptionAdapter;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FriendListActivity extends AppCompatActivity {
    private String username;
    private Button searchBtn;
    private Button toInitBtn;
    private EditText searchText;
    private ImageButton speakBtn;
//    private Button addFriendBtn;
    private CustomAdapter adapter;
    private ListView friendList;
//    private Spinner spinner;
    private OptionAdapter optionAdapter;

    private TextView spinnerTextView;
    private PopupWindow popupWindow;
    private ArrayList<String> friends;
    private String[] spinnerItems = {"添加好友","新朋友","创建群聊"};
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_friend_list);
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是好友列表界面");
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        friends=new ArrayList<>();
        friends=bundleExtra.getStringArrayList("friends");
        username= bundleExtra.getString("username");

        String nickname= bundleExtra.getString("nickname");
        String sex=bundleExtra.getString("sex");
        String address= bundleExtra.getString("address");
        String idCard=bundleExtra.getString("idCard");
        String phone= bundleExtra.getString("phone");
        searchBtn=findViewById(R.id.friendSearchBtn);
        searchText=findViewById(R.id.friendSearchText);
        speakBtn=findViewById(R.id.friendSpeakBtn);
//        addFriendBtn=findViewById(R.id.addFriendBtn);
        friendList=findViewById(R.id.friendListView);
        toInitBtn=findViewById(R.id.friendToInitBtn);
//        spinnerTextView = findViewById(R.id.spinner_textview);
//
//        // 设置TextView的长按监听事件
//        spinnerTextView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                // 在这里处理TextView的长按事件
//                Toast.makeText(FriendListActivity.this, "长按了下拉列表", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });
//
//        // 创建PopupWindow来显示下拉列表
//        View popupView = LayoutInflater.from(this).inflate(R.layout.spinner_popup, null);
//        popupWindow = new PopupWindow(popupView, spinnerTextView.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);

//        spinner=findViewById(R.id.FriendListSpinner);
        adapter = new CustomAdapter(this, friends,200,username,1);
        friendList.setAdapter(adapter);
//        String[] Array={"添加好友","新朋友","创建群聊"};
//        spinnerAdapter=new ArrayAdapter(this,R.layout.spinner_item,Array);
        spinnerTextView = findViewById(R.id.spinner_textview);

        // 设置TextView的长按监听事件
        spinnerTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 在这里处理TextView的长按事件
                Toast.makeText(FriendListActivity.this, "长按了下拉列表", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // 创建PopupWindow来显示下拉列表
        View popupView = LayoutInflater.from(this).inflate(R.layout.spinner_popup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // 创建适配器并填充下拉列表的选项
        optionAdapter = new OptionAdapter(this, R.layout.item_option, spinnerItems,username,friends,idCard,sex,nickname,address,phone);

        // 获取ListView
        ListView listView = popupView.findViewById(R.id.spinner_popup_listview);

        // 设置ListView的适配器
        listView.setAdapter(optionAdapter);

        // 设置ListView的点击事件监听器
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            mKqwSpeechSynthesizer.start("您选择了"+spinnerItems[position]+"选项。");
//        });
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItem = spinnerItems[position];
//                switch (selectedItem){
//                    case "添加好友":
//
//                        new Thread(getAllUserRun).start();
//                        break;
//                    case "新朋友":
//                        mKqwSpeechSynthesizer.start("成功！");
//                        break;
//                    case "创建群聊":
//
//                        break;
//                }
//                return true;
//            }
//        });

        // 显示下拉列表
        spinnerTextView.setOnClickListener(v -> {
            popupWindow.showAsDropDown(spinnerTextView);
        });

//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(spinnerAdapter);
//        registerForContextMenu(spinner);
//        ListView listView=popupView.findViewById(R.id.spinner_popup_listview);
//        listView.setAdapter(spinnerAdapter);
//        // 设置下拉列表的点击事件监听器
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItem = Array[position];
//                spinnerTextView.setText(selectedItem);
//                popupWindow.dismiss();
//            }
//        });

//        // 显示下拉列表
//        spinnerTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mKqwSpeechSynthesizer.start("点了");
//                popupWindow.showAsDropDown(spinnerTextView);
//
//            }
//        });
//    }
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String option=Array[position];
//                switch (option){
//                    case "添加好友":
//                        mKqwSpeechSynthesizer.start("这是添加好友选项");
//                        break;
////                        new Thread(getAllUserRun).start();
//                    case "新朋友":
//                        mKqwSpeechSynthesizer.start("这是添新朋友选项");
//                        break;
//                    case "创建群聊":
//                        mKqwSpeechSynthesizer.start("这是创建群聊选项");
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        spinner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//                return true;
//            }
//        });
//        addFriendBtn.setOnLongClickListener(new View.OnLongClickListener() {
//           @Override
//           public boolean onLongClick(View view) {
////               UserInfo userInfo=new UserInfo("xingye","xingye",null);
////               XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_KRY, userInfo).withContext(FriendListActivity.this).navigate();
//               new Thread(getAllUserRun).start();
//               return true;
//           }
//       });
        toInitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是返回首页的按钮");
            }
        });
        toInitBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(FriendListActivity.this,InitActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("username",username);
                bundle.putString("nickname",nickname);
                bundle.putString("sex",sex);
                bundle.putString("address",address);
                bundle.putString("idCard",idCard);
                bundle.putString("phone",phone);
                intent.putExtra("bundle",bundle);
                startActivity(intent);
                return true;
            }
        });
    }
    Runnable getAllUserRun = new Runnable() {
        @Override
        public void run() {

            User user1=new User();
            user1.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(user1);

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
                    Intent intent=new Intent(FriendListActivity.this,AddFriendActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    bundle.putStringArrayList("friends",friends);
                    intent.putExtra("bundle",bundle);
                    intent.putParcelableArrayListExtra("users", users);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//
//        if (v.getId() == R.id.FriendListSpinner) {
//            getMenuInflater().inflate(R.menu.spinner_context_menu, menu);
//        }
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//
//        if (item.getItemId() == R.id.menu_long_press) {
//            // 在这里处理长按事件
//            String selectedOption = spinner.getSelectedItem().toString();
//            Toast.makeText(this, "长按了选项：" + selectedOption, Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//        return super.onContextItemSelected(item);
//    }
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是好友列表界面");
    }
}