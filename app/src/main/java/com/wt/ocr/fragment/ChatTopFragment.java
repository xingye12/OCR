package com.wt.ocr.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wt.ocr.CommunityActivity;
import com.wt.ocr.R;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatTopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatTopFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button returnBtn;

    public ChatTopFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatTopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatTopFragment newInstance(String param1, String param2) {
        ChatTopFragment fragment = new ChatTopFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat_top, container, false);
        Bundle bundleExtra=this.getArguments();
        String friend= bundleExtra.getString("friend");
        String username= bundleExtra.getString("username");
        String nickname= bundleExtra.getString("nickname");
        String sex=bundleExtra.getString("sex");
        String address= bundleExtra.getString("address");
        String idCard=bundleExtra.getString("idCard");
        String phone= bundleExtra.getString("phone");
        returnBtn=view.findViewById(R.id.chatBackBtn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(getActivity());
                mKqwSpeechSynthesizer.start("这是返回按钮，长按将返回到社区首页");
            }
        });
        returnBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(getActivity(), CommunityActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("friend",friend);
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
        return view;
    }
}