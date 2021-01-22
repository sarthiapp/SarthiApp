package com.example.sarthi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneSignup extends AppCompatActivity {
    Button generateOtp;
    EditText etPhoneNumber;
    ProgressBar progressBar;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_signup);
        generateOtp=findViewById(R.id.button2);
        progressBar=findViewById(R.id.progressBar);
        etPhoneNumber=findViewById(R.id.Phone_no_ed);
        Intent intent1=getIntent();
        type=intent1.getStringExtra("type");
        generateOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etPhoneNumber.getText().toString().trim().isEmpty()){
                    Toast.makeText(PhoneSignup.this,"Please enter phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                generateOtp.setVisibility(View.INVISIBLE);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91" + etPhoneNumber.getText().toString(),
                        60,                           // Timeout duration

                        TimeUnit.SECONDS,
                        PhoneSignup.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                progressBar.setVisibility(View.GONE);
                                generateOtp.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                progressBar.setVisibility(View.GONE);
                                generateOtp.setVisibility(View.VISIBLE);
                                Toast.makeText(PhoneSignup.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                progressBar.setVisibility(View.GONE);
                                generateOtp.setVisibility(View.VISIBLE);
                                Intent intent=new Intent(PhoneSignup.this,PhoneVerify.class);
                                intent.putExtra("mobile",etPhoneNumber.getText().toString());
                                intent.putExtra("verificationId",verificationId);
                                intent.putExtra("type",type);
                                startActivity(intent);
                            }
                        }
                );

            }
        });

    }
}