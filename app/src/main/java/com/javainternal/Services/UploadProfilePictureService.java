package com.javainternal.Services;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UploadProfilePictureService {
    @Multipart
    @POST("upload-profile-picture")
    Call<ResponseBody> uploadProfilePicture(
            @Query("type") String type,
            @Part("uid") RequestBody uid,
            @Part MultipartBody.Part image
    );
}
