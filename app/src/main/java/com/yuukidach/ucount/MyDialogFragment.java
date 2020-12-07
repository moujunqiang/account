package com.yuukidach.ucount;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuukidach.ucount.model.BookItem;
import com.yuukidach.ucount.model.IOItem;

import org.litepal.crud.DataSupport;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDialogFragment extends DialogFragment {


    private FragmentManager manager;
    private FragmentTransaction transaction;

    private Button addCostBtn;
    private Button addEarnBtn;
    private Button clearBtn;
    private ImageButton addFinishBtn;
    private ImageButton addDescription;


    private ImageView bannerImage;
    private TextView bannerText;

    private TextView moneyText;

    private TextView words;

    private SimpleDateFormat formatItem = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    private SimpleDateFormat formatSum = new SimpleDateFormat("yyyy年MM月", Locale.CHINA);
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");



    private class ButtonListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            transaction = manager.beginTransaction();

            switch (view.getId()) {
                case R.id.add_cost_button:
                    addCostBtn.setTextColor(0xffff8c00); // 设置“支出“按钮为灰色
                    addEarnBtn.setTextColor(0xff908070); // 设置“收入”按钮为橙色
                    transaction.replace(R.id.item_fragment, new CostFragment());

                    break;
                case R.id.add_earn_button:
                    addEarnBtn.setTextColor(0xffff8c00); // 设置“收入“按钮为灰色
                    addCostBtn.setTextColor(0xff908070); // 设置“支出”按钮为橙色
                    transaction.replace(R.id.item_fragment, new EarnFragment());

                    break;
                case R.id.add_finish:
                    String moneyString = moneyText.getText().toString();
                    if (moneyString.equals("0.00") || GlobalVariables.getmInputMoney().equals("")) {
                        Toast.makeText(getContext(), "唔姆，你还没输入金额", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(GlobalVariables.getmDescription())) {
                        Toast.makeText(getContext(), "请输入备注", Toast.LENGTH_SHORT).show();
                        return;

                    }

                    putItemInData(Double.parseDouble(moneyText.getText().toString()));
                    calculatorClear();
                    break;
                case R.id.clear:
                    calculatorClear();
                    moneyText.setText("0.00");
                    break;
                case R.id.add_description:
                    Intent intent = new Intent(getContext(), AddDescription.class);
                    startActivity(intent);
            }

            transaction.commit();
        }
    }

    public void putItemInData(double money) {
        IOItem ioItem = new IOItem();
        BookItem bookItem = DataSupport.find(BookItem.class, GlobalVariables.getmBookId());
        String tagName = (String) bannerText.getTag();
        int tagType = (int) bannerImage.getTag();

        if (tagType < 0) {
            ioItem.setType(ioItem.TYPE_COST);
        } else ioItem.setType(ioItem.TYPE_EARN);

        ioItem.setName(bannerText.getText().toString());
        ioItem.setSrcName(tagName);
        ioItem.setMoney(money);
        ioItem.setTimeStamp(formatItem.format(new Date()));         // 存储记账时间
        ioItem.setDescription(GlobalVariables.getmDescription());
        ioItem.setBookId(GlobalVariables.getmBookId());
        ioItem.save();

        // 将收支存储在对应账本下
        bookItem.getIoItemList().add(ioItem);
        bookItem.setSumAll(bookItem.getSumAll() + money * ioItem.getType());
        bookItem.save();

        calculateMonthlyMoney(bookItem, ioItem.getType(), ioItem);

        // 存储完之后及时清空备注
        GlobalVariables.setmDescription("");
    }

    // 计算月收支
    public void calculateMonthlyMoney(BookItem bookItem, int money_type, IOItem ioItem) {
        String sumDate = formatSum.format(new Date());

        // 求取月收支类型
        if (bookItem.getDate().equals(ioItem.getTimeStamp().substring(0, 8))) {
            if (money_type == 1) {
                bookItem.setSumMonthlyEarn(bookItem.getSumMonthlyEarn() + ioItem.getMoney());
            } else {
                bookItem.setSumMonthlyCost(bookItem.getSumMonthlyCost() + ioItem.getMoney());
            }
        } else {
            if (money_type == 1) {
                bookItem.setSumMonthlyEarn(ioItem.getMoney());
                bookItem.setSumMonthlyCost(0.0);
            } else {
                bookItem.setSumMonthlyCost(ioItem.getMoney());
                bookItem.setSumMonthlyEarn(0.0);
            }
            bookItem.setDate(sumDate);
        }

        bookItem.save();
    }

    // 数字输入按钮
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void calculatorNumOnclick(View v) {
        Button view = (Button) v;
        String digit = view.getText().toString();
        String money = GlobalVariables.getmInputMoney();
        if (GlobalVariables.getmHasDot() && GlobalVariables.getmInputMoney().length() > 2) {
            String dot = money.substring(money.length() - 3, money.length() - 2);
            if (dot.equals(".")) {
                Toast.makeText(getContext(), "唔，已经不能继续输入了", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        GlobalVariables.setmInputMoney(money + digit);
        moneyText.setText(decimalFormat.format(Double.valueOf(GlobalVariables.getmInputMoney())));
    }

    // 清零按钮
    public void calculatorClear() {
        GlobalVariables.setmInputMoney("");
        GlobalVariables.setHasDot(false);
        GlobalVariables.setmDescription("");
    }

    // 小数点处理工作
    public void calculatorPushDot(View view) {
        if (GlobalVariables.getmHasDot()) {
            Toast.makeText(getContext(), "已经输入过小数点了 ━ω━●", Toast.LENGTH_SHORT).show();
        } else {
            GlobalVariables.setmInputMoney(GlobalVariables.getmInputMoney() + ".");
            GlobalVariables.setHasDot(true);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getContext());

        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.activity_add_item, null);
        addCostBtn = (Button) dialogView.findViewById(R.id.add_cost_button);
        addEarnBtn = (Button) dialogView.findViewById(R.id.add_earn_button);
        addFinishBtn = (ImageButton)dialogView. findViewById(R.id.add_finish);
        addDescription = (ImageButton)dialogView. findViewById(R.id.add_description);
        clearBtn = (Button) dialogView.findViewById(R.id.clear);
        words = (TextView)dialogView. findViewById(R.id.anime_words);

        // 设置按钮监听
        addCostBtn.setOnClickListener(new ButtonListener());
        addEarnBtn.setOnClickListener(new ButtonListener());
        addFinishBtn.setOnClickListener(new ButtonListener());
        addDescription.setOnClickListener(new ButtonListener());
        clearBtn.setOnClickListener(new ButtonListener());


        bannerText = (TextView)dialogView. findViewById(R.id.chosen_title);
        bannerImage = (ImageView) dialogView.findViewById(R.id.chosen_image);

        moneyText = (TextView) dialogView.findViewById(R.id.input_money_text);
        // 及时清零
        moneyText.setText("0.00");

        manager = getChildFragmentManager();

        transaction = manager.beginTransaction();
        transaction.replace(R.id.item_fragment, new CostFragment());
        transaction.commit();
        customizeDialog.setTitle("添加");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return customizeDialog.create();

    }

}