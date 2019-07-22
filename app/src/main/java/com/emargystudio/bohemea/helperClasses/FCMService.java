package com.emargystudio.bohemea.helperClasses;




import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.History.HistoryActivity;
import com.emargystudio.bohemea.LocalDataBases.AppExecutors;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class FCMService extends FirebaseMessagingService {


    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    private static final String RESERVATION_CHANNEL_ID ="reservation_channel";
    private NotificationManager notificationManager;


    SharedPreferenceManger sharedPreferenceManger;
    User user ;
    ArrayList<String> tokens = new ArrayList<>();
    String token;


    private Handler mHandler = new Handler();


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        token=s;
        Common.isNewToken = true;



        sharedPreferenceManger =  SharedPreferenceManger.getInstance(getApplicationContext());
        sharedPreferenceManger.storeDeviceToken(token);
        user = sharedPreferenceManger.getUserData();
        AppExecutors.getInstance().mainThread().execute(mStatusChecker);




    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {


            user=sharedPreferenceManger.getUserData();

            if (user.getUserName()!=null){

                tokens = sharedPreferenceManger.getUserTokens().getTokens();
                if (tokens.size()== 0){
                    registerToken(user.getUserId(),token);
                }else {
                    if (!tokens.contains(token)){
                        registerToken(user.getUserId(),token);
                    }
                }
                mHandler.removeCallbacks(mStatusChecker);

            }else {


                // 5 seconds by default, can be changed later
                int mInterval = 5000;
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
            }

    };


    public void registerToken(final int userId , final String token){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.register_token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")){


                                    String token = jsonObject.getString("fcmToken");
                                    tokens.add(token);
                                    sharedPreferenceManger.storeUserTokens(userId,tokens);

                                }

                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
            ){

                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> userData = new HashMap<>();
                    userData.put("user_id",String.valueOf(userId));
                    userData.put("token",token);

                    return  userData;
                }
            };//end of string Request

            VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sharedPreferenceManger =  SharedPreferenceManger.getInstance(getApplicationContext());
        user = sharedPreferenceManger.getUserData();
        String userIDString = remoteMessage.getData().get("user_id");
        if (userIDString!=null) {
            if (Integer.parseInt(userIDString) == user.getUserId()) {
                notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                int notificationId = new Random().nextInt(60000);

                NotificationCompat.Builder notificationBuilder;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    setupChannels();
                    if (remoteMessage.getData().get("android_channel_id").equals(RESERVATION_CHANNEL_ID)) {

                        String res_id = remoteMessage.getData().get("res_id");
                        String user_id = remoteMessage.getData().get("user_id");
                        String result = remoteMessage.getData().get("result");

                        Intent intent = new Intent(this, HistoryActivity.class);
                        intent.putExtra("res_id",res_id);
                        intent.putExtra("user_id",user_id);
                        intent.putExtra("result",result);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                        stackBuilder.addNextIntentWithParentStack(intent);
                        PendingIntent resultPendingIntent =  stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        notificationBuilder = getNotificationBuilder(remoteMessage, resultPendingIntent, RESERVATION_CHANNEL_ID);
                    } else {
                        PendingIntent resultPendingIntent = getPendingIntent(MainActivity.class);
                        notificationBuilder = getNotificationBuilder(remoteMessage, resultPendingIntent, ADMIN_CHANNEL_ID);
                    }
                } else {
                    PendingIntent resultPendingIntent = getPendingIntent(MainActivity.class);
                    notificationBuilder = getNotificationBuilder(remoteMessage, resultPendingIntent, ADMIN_CHANNEL_ID);
                }

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());
            }
        }

    }

    private NotificationCompat.Builder getNotificationBuilder(RemoteMessage remoteMessage, PendingIntent resultPendingIntent, String reservationChannelId) {
        return new NotificationCompat.Builder(this, reservationChannelId)
                .setSmallIcon(R.drawable.logo)  //a resource for your custom small icon
                .setContentTitle(remoteMessage.getData().get("title")) //the "title" value you sent in your notification
                .setContentText(remoteMessage.getData().get("message")) //ditto
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
    }

    private PendingIntent getPendingIntent(Class intentClass) {
        Intent resultIntent = new Intent(this, intentClass);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }

        CharSequence reservationChannelName = getString(R.string.notification_reservation_channel_name);
        String reservationChannelDescription = getString(R.string.notification_reservation_channel_description);
        NotificationChannel reservationChannel;
        reservationChannel = new NotificationChannel(RESERVATION_CHANNEL_ID, reservationChannelName, NotificationManager.IMPORTANCE_HIGH);
        reservationChannel.setDescription(reservationChannelDescription);
        reservationChannel.enableLights(true);
        reservationChannel.setLightColor(Color.WHITE);
        reservationChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(reservationChannel);
        }
    }





}
