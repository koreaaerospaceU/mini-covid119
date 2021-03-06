package com.example.covid_19_noti_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.app.AlarmManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity  {
    AlarmManager alarm_manager;
    PendingIntent pendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final Calendar calendar = Calendar.getInstance();
        final Intent my_intent = new Intent(this, BroadcastD.class);
        Button alarm_on = findViewById(R.id.btn_start);
        alarm_on.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //바로 알림 푸쉬
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

                // reveiver에 string 값 넘겨주기
                my_intent.putExtra("state","alarm on");

                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, my_intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                // 알람셋팅
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        //API 19 이상 API 23미만
                        alarm_manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent) ;
                    } else {
                        //API 19미만
                        alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                } else {
                    //API 23 이상
                    alarm_manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        });

        // 알림 정지 버튼
        Button alarm_off = findViewById(R.id.btn_finish);
        alarm_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Alarm 종료", Toast.LENGTH_SHORT).show();
                // 알람매니저 취소
                alarm_manager.cancel(pendingIntent);

                my_intent.putExtra("state","alarm off");

                // 알람취소
                sendBroadcast(my_intent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    createPost();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    private void createPost() throws IOException {
        String region = null;
        String infect = null;
        final ArrayList<String> region_arr=new ArrayList<>();
        ArrayList<String> infect_arr=new ArrayList<>();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://172.30.1.72:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitService retrofitService=retrofit.create(RetrofitService.class);
        Call<JsonArray> call = retrofitService.getretrofitquery(region,infect);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    //Log.d("check : ","response.raw :"+response.body());
                    //Log.d("check : ","response.raw :"+postResult.getregion().get(0).getRegion());
                    //Log.d("check : ","response.raw :"+response.body());
                    String Name1="";
                    String Name2="";
                    String total_text="";
                    JsonArray jsonArray=response.body();
                    for(int i=0;i<jsonArray.size();i++) {
                        JsonElement jsonElement1 = jsonArray.get(i);
                        region_arr.add(jsonElement1.getAsJsonObject().get("region").getAsString());
                        Name1 = jsonElement1.getAsJsonObject().get("region").getAsString();
                        Name2 = jsonElement1.getAsJsonObject().get("new_infected").getAsString();
                        total_text = total_text + "\n" + Name1 + " : " +Name2;
                        Log.d("check : ","지역 :"+Name1 + "코로나 증가자 : " +Name2);
                    }
                    TextView textView=findViewById(R.id.textView);
                    textView.setText(total_text);
                    Log.d("check_total : ",total_text);

                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
            }
        });

    }
}
