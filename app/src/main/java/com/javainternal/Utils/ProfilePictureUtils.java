package com.javainternal.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.javainternal.R;
import com.javainternal.Services.UploadProfilePictureService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfilePictureUtils {

    public interface OnProfileUploadListener {
        void onUploadSuccess();
        void onUploadFailure();
    }

    public static void uploadProfilePicture(Context context, Uri imageUri, String uid, String type, OnProfileUploadListener listener) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(context, "Unable to open image", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] imageBytes = readBytes(inputStream);
            RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("profile", "image.jpg", requestFile);

            RequestBody uidPart = RequestBody.create(uid, MediaType.parse("text/plain"));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.backend_url) + "/upload/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UploadProfilePictureService service = retrofit.create(UploadProfilePictureService.class);

            Call<ResponseBody> call = service.uploadProfilePicture(type, uidPart, body);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show();
                        listener.onUploadSuccess();
                    } else {
                        Toast.makeText(context, "Server error!", Toast.LENGTH_SHORT).show();
                        listener.onUploadFailure();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Upload failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    listener.onUploadFailure();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
        }
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
