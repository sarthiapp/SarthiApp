package com.example.sarthi.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAsrDoCIc:APA91bGmvHK1pcL2QmS1oiNsLhdg4_Vz1oZSRKWG5CcqnpqGbr5z4-65a30_oPd407H-qjpWj8gWbSOCKVUbEwE8LI7Vwzcrrp1kjlCFYdnUWtu0dqDTCsWmmjh3_aM4Mnhc2UREM9LB" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

