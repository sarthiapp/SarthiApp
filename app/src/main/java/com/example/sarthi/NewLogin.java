package com.example.sarthi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewLogin extends AppCompatActivity {
    private static final int RC_SIGN_IN =123 ;
    EditText etEmailLogin,etPasswordLogin;
    Button btnLogin;
    ImageView btnGoogleLogin;
    TextView btnSignUp;
    String type;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);
        etEmailLogin=findViewById(R.id.etEmailLogin);
        etPasswordLogin=findViewById(R.id.etPasswordLogin);
        btnLogin=findViewById(R.id.btnLogin);
        btnGoogleLogin=findViewById(R.id.btnGLogin);
        btnSignUp=findViewById(R.id.btnSignUp);
        ////GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            ////    .requestIdToken(getString(R.string.default_web_client_id))
                ////.requestEmail()
               // .build();
        //mGoogleSignInClient = GoogleSignIn.getClient( this,gso);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidations())
                {
                    firebaseAuth.signInWithEmailAndPassword(etEmailLogin.getText().toString(), etPasswordLogin.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            FirebaseUser user = firebaseAuth.getCurrentUser();
                                            loginType(user);
                                        }
                                        else
                                        {
                                            Toast.makeText(NewLogin.this, "Please verify Your Email First", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(NewLogin.this, "This Email is Not Register Create A New Account", Toast.LENGTH_SHORT).show();
                                        // ...
                                    }

                                    // ...
                                }
                            });
                }
            }
        });
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(NewLogin.this,MainActivity.class);
                startActivity(intent);
            }
        });


    }
    private boolean checkValidations() {
        if(TextUtils.isEmpty(etEmailLogin.getText().toString())){
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(etPasswordLogin.getText().toString())){
            Toast.makeText(this, "Create Your Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this, "could not sign in", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(NewLogin.this, "Sign In successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            loginType(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(NewLogin.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    private void loginType(FirebaseUser user)
    {
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection("data").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        type=document.get("type").toString();
                        if(type.equals("user"))
                        {
                            Intent intent=new Intent(NewLogin.this,RiderActivity.class);
                            startActivity(intent);
                        }
                        else if(type.equals("driver"))
                        {
                            Intent intent=new Intent(NewLogin.this,DriverActivity.class);
                            startActivity(intent);
                        }
                        else if(type.equals("Attendant"))
                        {
                            Intent intent=new Intent(NewLogin.this,Attendant_Home.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(NewLogin.this, "No such Document Exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewLogin.this, "get failed with "+ task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}