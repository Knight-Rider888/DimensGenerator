package com.sample.dimens;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.fonuhuolian.xappwindows.XPermissionsNoticeWindow;
import org.fonuhuolian.xappwindows.bean.XPermissionNoticeBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Integer> size = new ArrayList<>(Arrays.asList(360, 375, 392, 411, 640, 768));
    private List<String> numbers = Arrays.asList("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");

    private TextView tv;
    private EditText et;
    private Button btn;

    private XPermissionsNoticeWindow permissionsWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);
        et = findViewById(R.id.et);
        btn = findViewById(R.id.btn);

        permissionsWindow = new XPermissionsNoticeWindow(this, Collections.singletonList(new XPermissionNoticeBean(R.drawable.eg_storage_permission, "存储", "写入文件", Manifest.permission.WRITE_EXTERNAL_STORAGE)), new XPermissionsNoticeWindow.Listener() {
            @Override
            public void onGranted() {

            }
        });
        permissionsWindow.start();
    }

    public void save(View view) {

        btn.setEnabled(false);

        String uiSize = et.getText().toString();

        if (TextUtils.isEmpty(uiSize)) {
            btn.setEnabled(true);
            Toast.makeText(this, "请输入设计图宽度尺寸(dp)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设计图尺寸
        int uiSizeInt = 0;

        try {
            uiSizeInt = Integer.parseInt(uiSize);
        } catch (Exception e) {
            Toast.makeText(this, "获取设计图宽度尺寸(dp)出错", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!size.contains(uiSizeInt)) {
            size.add(uiSizeInt);
        }

        tv.setText("");

        for (int i = 0; i < size.size(); i++) {

            // 需要生成的size
            Integer sizeInt = size.get(i);

            // 保存 当前对应sw的集合
            List<String> s = new ArrayList<>();

            for (float j = 0.5f; j < uiSizeInt + 0.5; j += 0.5f) {

                if (j == 0.5f) {

                    s.add("\t<!-- int value -->");
                    s.add("\r");

                    for (int k = 0; k < numbers.size(); k++) {

                        double f = 1.0f * sizeInt * (k + 1) / uiSizeInt;

                        DecimalFormat decimalFormat = new DecimalFormat();
                        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                        decimalFormat.setMaximumFractionDigits(0);
                        String f1 = decimalFormat.format(f);

                        if (Integer.parseInt(f1) == 0)
                            f1 = "1";

                        s.add("\t<integer name=\"int_" + (k + 1) + "val\">" + f1 + "</integer>");
                        s.add("\n");
                    }
                    s.add("\r");
                    s.add("\t<!-- dp value -->");
                    s.add("\r");

                    for (float k = 0.5f; k < uiSizeInt + 0.5; k += 0.5f) {

                        double f = k * sizeInt / uiSizeInt;
                        DecimalFormat decimalFormat = new DecimalFormat();
                        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
                        decimalFormat.setMaximumFractionDigits(2);
                        String f1 = decimalFormat.format(f);

                        String temp = "";

                        if ((k / 0.5) % 2 == 0) {
                            temp = ((int) k) + "";
                        } else {
                            temp = k + "";
                        }

                        s.add("\t<dimen name=\"dimen_negative_" + temp + "dp\">-" + f1 + "dp</dimen>");
                        s.add("\n");
                    }

                    // 获取转换值
                    double f = 1.0f * sizeInt * 0.3 / uiSizeInt;
                    DecimalFormat decimalFormat = new DecimalFormat();
                    decimalFormat.setRoundingMode(RoundingMode.FLOOR);
                    decimalFormat.setMaximumFractionDigits(2);
                    String f1 = decimalFormat.format(f);

                    s.add("\t<dimen name=\"dimen_" + 0.3 + "dp\">" + f1 + "dp</dimen>");
                    s.add("\n");
                }

                // 获取转换值
                double f = j * sizeInt / uiSizeInt;
                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setRoundingMode(RoundingMode.FLOOR);
                decimalFormat.setMaximumFractionDigits(2);
                String f1 = decimalFormat.format(f);


                String temp = "";

                if ((j / 0.5) % 2 == 0) {
                    temp = ((int) j) + "";
                } else {
                    temp = j + "";
                }

                s.add("\t<dimen name=\"dimen_" + temp + "dp\">" + f1 + "dp</dimen>");
                s.add("\n");
            }

            StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            builder.append("<resources>\n");
            builder.append("\r");


            for (int k = 0; k < s.size(); k++) {
                builder.append(s.get(k));
            }

            builder.append("\r");
            builder.append("</resources>");

            save(sizeInt, builder.toString());
        }


        btn.setEnabled(true);
        tv.setText("文件位置：" + getDir() + "/dimens");
    }

    // 获取生成目录
    private String getDir() {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        new File(dir).mkdirs();
        return dir;
    }

    // 存储路径
    private String getPath(int uiSizeInt) {
        String path = getDir() + File.separator + "dimens" + File.separator + File.separator + ("values-sw" + uiSizeInt + "dp") + File.separator;

        File file = new File(path);
        file.mkdirs();
        return path;
    }

    // 删除存储路径
    private void deletePath(int uiSizeInt) {
        String path = getDir() + File.separator + "dimens" + File.separator + ("values-sw" + uiSizeInt + "dp") + File.separator;
        // 如果dir对应的文件不存在，则退出
        File dirFile = new File(path);

        if (!dirFile.exists()) {
            return;
        }

        if (!dirFile.isFile()) {
            for (File file : dirFile.listFiles()) {
                file.delete();
            }
        }
    }

    // 保存到文件
    private void save(int sizeInt, String inputText) {

        // 删除文件夹下内容
        deletePath(sizeInt);
        // 获取文件夹
        String folder = getPath(sizeInt);

        File file = new File(folder, "dimens.xml");


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            //true表示在文件末尾追加
            fos.write(inputText.getBytes());
            fos.close();
            scanMediaCenter(file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void scanMediaCenter(String filePath) {
        Uri uri = Uri.fromFile(new File(filePath));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsWindow.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionsWindow.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        permissionsWindow.onDestroy();
    }
}