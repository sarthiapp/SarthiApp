package com.example.sarthi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyProfileFragment extends Fragment {


    TextView tvTypeDriver,tvNameDriver,tvModelDriver,tvNumberDriver,tvEmailDriver,tvPhoneDriver,tvRadiusDriver;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_my_profile, container, false);

        tvTypeDriver=v.findViewById(R.id.tvTypeDriver);
        tvNameDriver=v.findViewById(R.id.tvNameDriver);
        tvModelDriver=v.findViewById(R.id.tvModelDriver);
        tvNumberDriver=v.findViewById(R.id.tvNumberDriver);
        tvEmailDriver=v.findViewById(R.id.tvEmailDriver);
        tvPhoneDriver=v.findViewById(R.id.tvPhoneDriver);
        tvRadiusDriver=v.findViewById(R.id.tvRadiusDriver);

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference dref=db.collection("data").document(firebaseUser.getUid());
        dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists())
                    {
                        tvTypeDriver.setText(document.get("type").toString());
                        tvNameDriver.setText(document.get("name").toString());
                        tvModelDriver.setText(document.get("car model").toString());
                        tvNumberDriver.setText(document.get("car number").toString());
                        tvPhoneDriver.setText(document.get("phone").toString());
                        tvEmailDriver.setText(document.get("email").toString());
                        tvRadiusDriver.setText(document.get("radius").toString());
                    }
                }
            }
        });
        return v;
    }
}
