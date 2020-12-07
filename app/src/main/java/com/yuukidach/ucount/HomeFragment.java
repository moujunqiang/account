package com.yuukidach.ucount;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuukidach.ucount.model.BookItem;
import com.yuukidach.ucount.model.BookItemAdapter;
import com.yuukidach.ucount.model.IOItem;
import com.yuukidach.ucount.model.IOItemAdapter;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {
    private List<IOItem> ioItemList = new ArrayList<>();
    private List<BookItem> bookItemList = new ArrayList<>();

    private RecyclerView ioItemRecyclerView;
    private IOItemAdapter ioAdapter;
    private Button showBtn;
    private ImageView headerImg;
    private TextView monthlyCost, monthlyEarn;

    // parameter for drawer
    private DrawerLayout drawerLayout;
    private LinearLayout bookLinearLayout;
    private RecyclerView bookItemRecyclerView;
    private BookItemAdapter bookAdapter;
    private ImageButton addBookButton;
    private ImageView drawerBanner;

    public static String PACKAGE_NAME;
    public static Resources resources;
    public static final int SELECT_PIC4MAIN = 1;
    public static final int SELECT_PIC4DRAWER = 2;
    public DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private static final String TAG = "HomeFragment";
    private SimpleDateFormat formatSum = new SimpleDateFormat("yyyy年MM月", Locale.CHINA);
    String sumDate = formatSum.format(new Date());

    private View inflate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflate = inflater.inflate(R.layout.activity_main, container, false);
        initView();
        return inflate;
    }

    protected void initView() {


        // litepal
        Connector.getDatabase();

        // 获得包名和资源，方便后面的程序使用
        PACKAGE_NAME = getContext().getApplicationContext().getPackageName();
        resources = getResources();

        showBtn = (Button) inflate.findViewById(R.id.show_money_button);
        ioItemRecyclerView = (RecyclerView) inflate.findViewById(R.id.in_and_out_items);
        headerImg = (ImageView) inflate.findViewById(R.id.header_img);
        monthlyCost = (TextView) inflate.findViewById(R.id.monthly_cost_money);
        monthlyEarn = (TextView) inflate.findViewById(R.id.monthly_earn_money);
        // drawer
        drawerLayout = (DrawerLayout) inflate.findViewById(R.id.drawer_of_books);
        bookItemRecyclerView = (RecyclerView) inflate.findViewById(R.id.book_list);
        addBookButton = (ImageButton) inflate.findViewById(R.id.add_book_button);
        bookLinearLayout = (LinearLayout) inflate.findViewById(R.id.left_drawer);
        drawerBanner = (ImageView) inflate.findViewById(R.id.drawer_banner);

        // 设置按钮监听
        showBtn.setOnClickListener(new ButtonListener());
        addBookButton.setOnClickListener(new ButtonListener());

        // 设置首页header图片长按以更换图片
        headerImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectPictureFromGallery(1);
                return false;
            }
        });

        drawerBanner.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectPictureFromGallery(2);
                return false;
            }
        });

        setImageForHeaderAndBanner();
    }


    @Override
    public void onResume() {
        super.onResume();

        initBookItemList(getContext());
        initIoItemList(getContext());

        showBtn.setText("显示余额");

        BookItem tmp = DataSupport.find(BookItem.class, bookItemList.get(GlobalVariables.getmBookPos()).getId());
        monthlyCost.setText(decimalFormat.format(tmp.getSumMonthlyCost()));
        monthlyEarn.setText(decimalFormat.format(tmp.getSumMonthlyEarn()));
    }


    // 各个按钮的活动
    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {


                case R.id.show_money_button:
                    if (showBtn.getText() == "显示余额") {
                        BookItem tmp = DataSupport.find(BookItem.class, GlobalVariables.getmBookId());

                        String sumString = decimalFormat.format(tmp.getSumAll());
                        showBtn.setText(sumString);
                    } else showBtn.setText("显示余额");
                    break;
                case R.id.add_book_button:
                    final BookItem bookItem = new BookItem();
                    final EditText book_title = new EditText(getContext());
                    // 弹窗输入
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("请输入新的账本名字");

                    builder.setView(book_title);

                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!book_title.getText().toString().isEmpty()) {
                                bookItem.setName(book_title.getText().toString());
                                bookItem.setSumAll(0.0);
                                bookItem.setSumMonthlyCost(0.0);
                                bookItem.setSumMonthlyEarn(0.0);
                                bookItem.setDate(sumDate);
                                bookItem.save();

                                onResume();
                            } else
                                Toast.makeText(getContext(), "没有输入新账本名称哦", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();  // 显示弹窗
                    break;

                default:
                    break;
            }
        }
    }


    // 初始化收支项目显示
    public void initIoItemList(final Context context) {

        ioItemList = DataSupport.where("bookId = ?", String.valueOf(GlobalVariables.getmBookId())).find(IOItem.class);
        setIoItemRecyclerView(context);
    }


    public void initBookItemList(final Context context) {
        bookItemList = DataSupport.findAll(BookItem.class);

        if (bookItemList.isEmpty()) {
            BookItem bookItem = new BookItem();

            bookItem.saveBook(bookItem, 1, "默认账本");
            bookItem.setSumAll(0.0);
            bookItem.setSumMonthlyCost(0.0);
            bookItem.setSumMonthlyEarn(0.0);
            bookItem.setDate(sumDate);
            bookItem.save();

            bookItemList = DataSupport.findAll(BookItem.class);
        }

        setBookItemRecyclerView(context);
    }

    public void selectPictureFromGallery(int id) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // 设置选择类型为图片类型
        intent.setType("image/*");
        // 打开图片选择
        if (id == 1)
            startActivityForResult(intent, SELECT_PIC4MAIN);
        else
            startActivityForResult(intent, SELECT_PIC4DRAWER);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PIC4MAIN:
                if (data == null) return;
                // 用户从图库选择图片后会返回所选图片的Uri
                Uri uri1 = data.getData();
                this.headerImg.setImageURI(uri1);
                saveImageUri(SELECT_PIC4MAIN, uri1);

                // 获取永久访问图片URI的权限
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getActivity().getContentResolver().takePersistableUriPermission(uri1, takeFlags);
                break;

            case SELECT_PIC4DRAWER:
                if (data == null) return;
                // 用户从图库选择图片后会返回所选图片的Uri
                Uri uri2 = data.getData();
                this.drawerBanner.setImageURI(uri2);
                saveImageUri(SELECT_PIC4DRAWER, uri2);

                // 获取永久访问图片URI的权限
                int takeFlags2 = data.getFlags();
                takeFlags2 &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getActivity().getContentResolver().takePersistableUriPermission(uri2, takeFlags2);
                break;
        }
    }

    // 利用SharedPreferences保存图片uri
    public void saveImageUri(int id, Uri uri) {
        SharedPreferences pref = getActivity().getSharedPreferences("image" + id, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString("uri", uri.toString());
        prefEditor.apply();
    }

    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setImageForHeaderAndBanner() {
        SharedPreferences pref1 = getActivity().getSharedPreferences("image" + SELECT_PIC4MAIN, MODE_PRIVATE);
        String imageUri1 = pref1.getString("uri", "");

        if (!imageUri1.equals("")) {
            Uri contentUri = Uri.parse(imageUri1);
            this.headerImg.setImageURI(contentUri);
        }

        SharedPreferences pref2 = getActivity().getSharedPreferences("image" + SELECT_PIC4DRAWER, MODE_PRIVATE);
        String imageUri2 = pref2.getString("uri", "");

        if (!imageUri2.equals("")) {
            Uri contentUri = Uri.parse(imageUri2);
            this.drawerBanner.setImageURI(contentUri);
        }
    }

    public void setIoItemRecyclerView(Context context) {
        // 用于存储recyclerView的日期
        GlobalVariables.setmDate("");

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);    // 列表从底部开始展示，反转后从上方开始展示
        layoutManager.setReverseLayout(true);   // 列表反转

        ioItemRecyclerView.setLayoutManager(layoutManager);
        ioAdapter = new IOItemAdapter(ioItemList);
        ioItemRecyclerView.setAdapter(ioAdapter);
        ioAdapter.setOnLongClick(new IOItemAdapter.OnLongClick() {
            @Override
            public void onLoginClick(final int position) {
                // 弹窗确认
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("你确定删除？");

                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ioAdapter.removeItem(position);
                        // 刷新界面
                        onResume();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();  // 显示弹窗
            }

            @Override
            public void onItemClick(final IOItem position) {
                AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getContext());

                final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.detail_dialog, null);
                // 获取EditView中的输入内容
                TextView tvTime = (TextView) dialogView.findViewById(R.id.tv_time);
                TextView tvDesc = (TextView) dialogView.findViewById(R.id.tv_desc);
                TextView tvMoney = (TextView) dialogView.findViewById(R.id.tv_money);
                tvTime.setText(position.getTimeStamp());
                tvDesc.setText("备注："+position.getDescription());
                String type = position.getType() == -1 ? "支出：" : "收入：";
                tvMoney.setText(type + position.getMoney());
                customizeDialog.setTitle("详情");
                customizeDialog.setView(dialogView);
                customizeDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                customizeDialog.show();
            }
        });
    }

    public void setBookItemRecyclerView(Context context) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);

        bookItemRecyclerView.setLayoutManager(layoutManager);
        bookItemRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        bookAdapter = new BookItemAdapter(bookItemList);

        bookAdapter.setOnItemClickListener(new BookItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 选中之后的操作
                GlobalVariables.setmBookPos(position);
                GlobalVariables.setmBookId(bookItemList.get(position).getId());
                onResume();
                drawerLayout.closeDrawer(bookLinearLayout);
            }
        });

        bookItemRecyclerView.setAdapter(bookAdapter);
        bookAdapter.setOnItemLongClick(new BookItemAdapter.OnItemLongClick() {
            @Override
            public void onLongClick(final int position) {
                // 弹窗确认
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("你确定要删除么？");

                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bookAdapter.removeItem(position);
                        // 刷新界面
                        onResume();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();  // 显示弹窗
            }
        });

        //GlobalVariables.setmBookId(bookItemRecyclerView.getId());
    }
}