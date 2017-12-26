package com.example.administrator.jsonweather39;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class JsonWeatherActivity extends AppCompatActivity {
    private String cityname="广州";
    HttpURLConnection httpConn=null;
    InputStream din =null;
    private EditText mCityname;
    private Button mSearch;
    private TextView mShowWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_weather);
        setTitle("天气预报JSON");
        mCityname=(EditText)findViewById(R.id.cityname);
        mSearch=(Button)findViewById(R.id.search);
        mShowWeather=(TextView)findViewById(R.id.show_weather);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowWeather.setText("");
                cityname=mCityname.getText().toString();
                Toast.makeText(JsonWeatherActivity.this,"正在查询天气...",Toast.LENGTH_LONG).show();
                GetJson gj=new GetJson(cityname);
                gj.start();
            }
        });
    }
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    showData((String)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private  void showData(String jData){
        try {
            JSONObject jobj = new JSONObject(jData);
            JSONObject weather = jobj.getJSONObject("data");
            StringBuffer wbf = new StringBuffer();
            wbf.append("当前温度："+weather.getString("wendu")+"\n");
            wbf.append("天气提示："+weather.getString("ganmao")+"\n");
            JSONArray jary = weather.getJSONArray("forecast");
            for(int i=0;i<jary.length();i++){
                JSONObject pobj = (JSONObject)jary.opt(i);
                wbf.append("日期："+pobj.getString("date")+"\n");
                wbf.append("最高温："+pobj.getString("high")+"\n");
                wbf.append("最低温："+pobj.getString("low")+"\n");
                wbf.append("风向："+pobj.getString("fengxiang"));
                wbf.append("风力："+pobj.getString("fengli")+"\n");
                wbf.append("天气："+pobj.getString("type")+"\n");
            }
            mShowWeather.setText(wbf.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
    class GetJson extends Thread{
        private String urlstr =  "http://wthrcdn.etouch.cn/weather_mini?city=";
        public GetJson(String cityname){
            try{
                urlstr = urlstr+ URLEncoder.encode(cityname,"UTF-8");
            }catch (Exception ee){
                ee.printStackTrace();
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
                msg.what = 1;
                handler.sendMessage(msg);
                Looper.prepare();
                Toast.makeText(JsonWeatherActivity.this,"获取数据成功",Toast.LENGTH_LONG).show();
                Looper.loop();
            }catch (Exception ee){
                Looper.prepare();
                Toast.makeText(JsonWeatherActivity.this,"获取数据失败，网络连接失败或输入有误",Toast.LENGTH_LONG).show();
                Looper.loop();
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
