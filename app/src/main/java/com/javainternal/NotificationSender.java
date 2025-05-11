//package com.javainternal;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//
//import java.io.FileInputStream;
//
//public class NotificationSender {
//    public static void main(String[] args) throws Exception {
//        // Initialize Firebase Admin SDK
//        FileInputStream serviceAccount = new FileInputStream("path/to/serviceAccountKey.json");
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//        FirebaseApp.initializeApp(options);
//
//        // Send notification
//        String receiverToken = "RECEIVER_FCM_TOKEN"; // Replace with the receiver's FCM token
//        String title = "New Message";
//        String body = "You have received a new message!";
//        Message message = Message.builder()
//                .setToken(receiverToken)
//                .setNotification(Notification.builder()
//                        .setTitle(title)
//                        .setBody(body)
//                        .build())
//                .build();
//
//        String response = FirebaseMessaging.getInstance().send(message);
//        System.out.println("Notification sent successfully: " + response);
//    }
//}