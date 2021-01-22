package com.example.sarthi;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link new_home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class new_home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public new_home() {
        // Required empty public constructor
    }

    LinearLayout home_user,home_attendant;
    Button CabBooking;
    public static new_home newInstance(String param1, String param2) {
        new_home fragment = new new_home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_new_home, container, false);
        home_user=v.findViewById(R.id.home_user);
        home_attendant=v.findViewById(R.id.home_attendant);
        CabBooking=v.findViewById(R.id.CabBooking);
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
                        set(document);
                    }
                }
            }
        });
        CabBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRiderActivity();
            }
        });
        return v;
    }

    private void set(DocumentSnapshot document) {
        if(document.get("type").toString().equals("user"))
        {
            home_user.setVisibility(View.VISIBLE);
        }
        else if(document.get("type").toString().equals("attendant"))
        {
            home_attendant.setVisibility(View.VISIBLE);
        }
    }

    public void openRiderActivity() {

        Intent intent = new Intent(getContext(), RiderActivity.class);
        startActivity(intent);
    }
}