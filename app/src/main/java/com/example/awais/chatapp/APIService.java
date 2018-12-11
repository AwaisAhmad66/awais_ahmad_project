package com.example.awais.chatapp;

import com.example.awais.chatapp.Notifications.MyResponse;
import com.example.awais.chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAAXdK2qw:APA91bEwtqt-AWIq9PucgNXUER0Sb3JBkbnIB7FoZOAkrlRrFJaexWIVrukQ-3flrFXQnJ2YgdcQdVnzqNnHw4CHqksgarKfKs397aWVPXH7vRDIGh9OJ_O4LpENAUzu1O3aIxUb1sUp"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
