package com.yuukidach.ucount;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import at.markushi.ui.CircleButton;

/**
 * 添加备注信息
 */
public class AddDescription extends AppCompatActivity {
    private EditText inputTxt;
    private TextView countTxt;
    private TextView dateTxt;
    private CircleButton doneBtn;

    private SimpleDateFormat formatItem = new SimpleDateFormat("yyyy年MM月dd日");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_descrpition);

        inputTxt = (EditText) findViewById(R.id.page3_edit);
        countTxt = (TextView) findViewById(R.id.page3_count);
        dateTxt = (TextView) findViewById(R.id.page3_date);
        doneBtn = (CircleButton) findViewById(R.id.page3_done);

        // 显示日期
        dateTxt.setText(formatItem.format(new Date()));
        final Calendar calendar = Calendar.getInstance();
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddDescription.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        dateTxt.setText(i + "年" + i1 + "月" + i2 + "日");
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 获取焦点
        inputTxt.setFocusable(true);

        inputTxt.setText(GlobalVariables.getmDescription());
        inputTxt.setSelection(inputTxt.getText().length());
        countTxt.setText(String.valueOf(inputTxt.getText().length()) + "/30");

        // 设置输入文本监听，实时显示字数
        inputTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countTxt.setText(String.valueOf(s.length()) + "/30");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalVariables.setmDescription(inputTxt.getText().toString());
                finish();
            }
        });
    }
}
