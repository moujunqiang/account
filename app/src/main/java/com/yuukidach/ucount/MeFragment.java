package com.yuukidach.ucount;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MeFragment extends Fragment {
    private View inflate;
    private LinearLayout llLogin;
    private TextView tvName;

    public static MeFragment newInstance() {

        Bundle args = new Bundle();

        MeFragment fragment = new MeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflate = inflater.inflate(R.layout.me_fragment, container, false);
        initView();
        return inflate;
    }

    public void initView() {
        String name = (String) SPUtils.get(getContext(), "name", "");
        llLogin = inflate.findViewById(R.id.ll_login);
        tvName = inflate.findViewById(R.id.my_nickname);
        tvName.setText(name);
        inflate.findViewById(R.id.my_uid).setVisibility(View.GONE);
        llLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        inflate.findViewById(R.id.login_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), LoginActivity.class));
                SPUtils.put(getContext(), "login", false);

            }
        });
        inflate.findViewById(R.id.main_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AboutActivity.class));


            }
        });
        inflate.findViewById(R.id.main_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SuggestActivity.class));


            }
        });
    }
}
