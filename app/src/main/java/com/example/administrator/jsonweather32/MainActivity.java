package com.example.administrator.jsonweather32;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;



public class MainActivity extends AppCompatActivity{
    HttpURLConnection httpConn = null;
    InputStream din =null;

    Button find = null;
    EditText value = null;
    TextView sdata = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气查询Json简单版");
        find = (Button)findViewById(R.id.find);
        value = (EditText)findViewById(R.id.value);
        value.setText("广州");//初始化，给个初值，方便测试
        sdata = (TextView)findViewById(R.id.showmydata);
        sdata.setMovementMethod(ScrollingMovementMethod.getInstance());

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sdata.setText("");//清空数据
                Toast.makeText(MainActivity.this, "正在查询天气信息", Toast.LENGTH_SHORT).show();
                GetJson gd = new GetJson(value.getText().toString());//调用线程类创建的对象
                gd.start();//运行线程对象

            }
        });

    }



    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 123:
                        showData((String)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private  void showData(String Jdata){
        try{

            JSONObject jsonObject = new JSONObject(Jdata);
            JSONObject data = jsonObject.getJSONObject("data");
            StringBuffer winf = new StringBuffer();
            winf.append("当前温度："+data.getString("wendu")+"℃\n");
            winf.append("天气提示："+data.getString("ganmao")+"\n");
            JSONArray forecast = data.getJSONArray("forecast");
            for(int i=0;i<forecast.length();i++){
                JSONObject perday = (JSONObject)forecast.opt(i);
                winf.append("日期："+perday.getString("date")+"\n");
                winf.append("风向："+perday.getString("fengxiang")+"\n");
                winf.append("风力："+perday.getString("fengli")+"\n");
                winf.append("温度范围："+perday.getString("low")+"℃--"+perday.getString("high")+"℃\n");
                winf.append("天气概况："+perday.getString("type")+"\n");

            }
            sdata.setText(winf.toString());
        }catch (Exception e){
            sdata.setText(e.toString());
        }
        //这里我直接显示json数据，没解析。解析的方法，请参考教材或网上相应的代码

    }
    class GetJson extends Thread{

        private String urlstr =  "http://wthrcdn.etouch.cn/weather_mini?city=";
        public GetJson(String cityname){
            try{
                urlstr = urlstr+URLEncoder.encode(cityname,"UTF-8");

            }catch (Exception ee){

            }
        }
        @Override
        public void run() {
            try {
                URL url = new URL(urlstr);
                httpConn = (HttpURLConnection)url.openConnection();
                httpConn.setRequestMethod("GET");
                din = httpConn.getInputStream();
                InputStreamReader in = new InputStreamReader(din);
                BufferedReader buffer = new BufferedReader(in);
                StringBuffer sbf = new StringBuffer();
                String line = null;
                while( (line=buffer.readLine())!=null) {
                    sbf.append(line);
                }
                Message msg = new Message();
                msg.obj = sbf.toString();
                msg.what = 123;
                handler.sendMessage(msg);
                Looper.prepare(); //在线程中调用Toast，要使用此方法，这里纯粹演示用:)
                Toast.makeText(MainActivity.this,"获取数据成功",Toast.LENGTH_LONG).show();
                Looper.loop(); //在线程中调用Toast，要使用此方法



            }catch (Exception ee){
                Looper.prepare(); //在线程中调用Toast，要使用此方法
                Toast.makeText(MainActivity.this,"获取数据失败，网络连接失败或输入有误",Toast.LENGTH_LONG).show();
                Looper.loop(); //在线程中调用Toast，要使用此方法
                ee.printStackTrace();
            }finally {
                try{
                    httpConn.disconnect();
                    din.close();

                }catch (Exception ee){
                    ee.printStackTrace();
                }
            }
        }
    }

}
