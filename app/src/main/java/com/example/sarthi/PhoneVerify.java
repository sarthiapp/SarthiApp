package com.example.sarthi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class PhoneVerify extends AppCompatActivity {
    Button btnVerify,resend;
    EditText otpNumberOne,getOtpNumberTwo,getOtpNumberThree,getOtpNumberFour,getOtpNumberFive,otpNumberSix;
    ProgressBar progressBar;
    private String verificationId;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);
        btnVerify = findViewById(R.id.verifyPhoneBTn);
        resend = findViewById(R.id.resendOTP);
        otpNumberOne = findViewById(R.id.otpNumberOne);
        getOtpNumberTwo = findViewById(R.id.optNumberTwo);
        getOtpNumberThree = findViewById(R.id.otpNumberThree);
        getOtpNumberFour = findViewById(R.id.otpNumberFour);
        getOtpNumberFive = findViewById(R.id.otpNumberFive);
        otpNumberSix = findViewById(R.id.optNumberSix);
        progressBar = findViewById(R.id.progressBar);

        setotpcursor();
        verificationId = getIntent().getStringExtra("verificationId");
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otpNumberOne.getText().toString().trim().isEmpty()
                        || getOtpNumberTwo.getText().toString().trim().isEmpty()
                        || getOtpNumberThree.getText().toString().trim().isEmpty()
                        || getOtpNumberFour.getText().toString().trim().isEmpty()
                        || getOtpNumberFive.getText().toString().trim().isEmpty()
                        || otpNumberSix.getText().toString().trim().isEmpty()) {
                    Toast.makeText(PhoneVerify.this, "Please enter Valid Code", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = otpNumberOne.getText().toString() + getOtpNumberTwo.getText().toString() + getOtpNumberThree.getText().toString() + getOtpNumberFour.getText().toString() +
                        getOtpNumberFive.getText().toString() + otpNumberSix.getText().toString();
                if (verificationId != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    btnVerify.setVisibility(View.INVISIBLE);
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationId,
                            code
                    );
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    btnVerify.setVisibility(View.VISIBLE);
                                    if (task.isSuccessful()) {
                                        sign_login();


                                    } else {
                                        Toast.makeText(PhoneVerify.this, "The Verification Code Was invalid", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }


            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91" + getIntent().getStringExtra("mobile"),
                        60,                           // Timeout duration

                        TimeUnit.SECONDS,
                        PhoneVerify.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(PhoneVerify.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                verificationId = s;
                                Toast.makeText(PhoneVerify.this, "Code sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

            }
        });
    }
    public void sign_login()
    {
        FirebaseAuth fauth=FirebaseAuth.getInstance();
        FirebaseUser fuser=fauth.getCurrentUser();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection("data").document(fuser.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        type=document.get("type").toString();
                        if(type.equals("user"))
                        {
                            Toast.makeText(PhoneVerify.this, "logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(PhoneVerify.this,User_Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else if(type.equals("driver"))
                        {
                            Toast.makeText(PhoneVerify.this, "logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(PhoneVerify.this,Driver_Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else if(type.equals("attendant"))
                        {
                            Toast.makeText(PhoneVerify.this, "logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(PhoneVerify.this,Attendant_Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                    }
                    else {
                        type=getIntent().getStringExtra("type");
                        if(type.equals("user"))
                        {
                            Intent intent=new Intent(PhoneVerify.this,SignUpUser.class);
                            startActivity(intent);
                        }
                        if(type.equals("driver"))
                        {
                            Intent intent=new Intent(PhoneVerify.this,SignUpDriver.class);
                            startActivity(intent);
                        }
                        if(type.equals("attendant"))
                        {
                            Intent intent=new Intent(PhoneVerify.this,SignUpAttendant.class);
                            startActivity(intent);
                        }
                    }
                }
                else {
                    Toast.makeText(PhoneVerify.this, "get failed with "+ task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setotpcursor() {
        otpNumberOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty())
                    getOtpNumberTwo.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getOtpNumberTwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty())
                    getOtpNumberThree.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty())
                    otpNumberOne.requestFocus();
            }
        });
        getOtpNumberThree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty())
                    getOtpNumberFour.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty())
                    getOtpNumberTwo.requestFocus();

            }
        });
        getOtpNumberFour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty())
                    getOtpNumberFive.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty())
                    getOtpNumberThree.requestFocus();

            }
        });
        getOtpNumberFive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty())
                    otpNumberSix.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty())
                    getOtpNumberFour.requestFocus();

            }
        });
        otpNumberSix.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty())
                    getOtpNumberFive.requestFocus();

            }
        });
    }

}