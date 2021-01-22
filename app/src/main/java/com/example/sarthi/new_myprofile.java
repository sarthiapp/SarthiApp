package com.example.sarthi;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link new_myprofile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class new_myprofile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public new_myprofile() {
        // Required empty public constructor
    }

    TextView tvTypeUser,tvNameUser,tvAgeUser,tvAddressUser,tvPhoneUser,tvEmailUser;
    TextView tvTypeAttendant,tvNameAttendant,tvGenderAttendant,tvEducationAttendant,tvEmailAttendant,tvPhoneAttendant,tvPastAttendant,tvAgeAttendant;
    LinearLayout layout_user,layout_attendant;
    // TODO: Rename and change types and number of parameters
    public static new_myprofile newInstance(String param1, String param2) {
        new_myprofile fragment = new new_myprofile();
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
        View v= inflater.inflate(R.layout.fragment_new_myprofile, container, false);
        tvTypeUser=v.findViewById(R.id.tvTypeUser);
        tvNameUser=v.findViewById(R.id.tvNameUser);
        tvAddressUser=v.findViewById(R.id.tvAddressUser);
        tvAgeUser=v.findViewById(R.id.tvAgeUser);
        tvPhoneUser=v.findViewById(R.id.tvPhoneUser);
        tvEmailUser=v.findViewById(R.id.tvEmailUser);
        tvTypeAttendant=v.findViewById(R.id.tvTypeAttendant);
        tvNameAttendant=v.findViewById(R.id.tvNameAttendant);
        tvGenderAttendant=v.findViewById(R.id.tvGenderAttendant);
        tvEducationAttendant=v.findViewById(R.id.tvEducationAttendant);
        tvEmailAttendant=v.findViewById(R.id.tvEmailAttendant);
        tvPhoneAttendant=v.findViewById(R.id.tvPhoneAttendant);
        tvPastAttendant=v.findViewById(R.id.tvPastAttendant);
        tvAgeAttendant=v.findViewById(R.id.tvAgeAttendant);
        layout_user=v.findViewById(R.id.layout_user);
        layout_attendant=v.findViewById(R.id.layout_attendant);
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

        return v;
    }
    private void set(DocumentSnapshot document) {
        if(document.get("type").toString().equals("user"))
        {
            layout_user.setVisibility(View.VISIBLE);
            tvTypeUser.setText(document.get("type").toString());
            tvNameUser.setText(document.get("name").toString());
            tvAgeUser.setText(document.get("age").toString());
            tvAddressUser.setText(document.get("address").toString());
            tvPhoneUser.setText(document.get("phone").toString());
            tvEmailUser.setText(document.get("email").toString());
        }
        else if(document.get("type").toString().equals("attendant"))
        {
            layout_attendant.setVisibility(View.VISIBLE);
            tvTypeAttendant.setText(document.get("type").toString());
            tvNameAttendant.setText(document.get("name").toString());
            tvGenderAttendant.setText(document.get("gender").toString());
            tvAgeAttendant.setText(document.get("age").toString());
            tvPhoneAttendant.setText(document.get("phone").toString());
            tvEmailAttendant.setText(document.get("email").toString());
            tvEducationAttendant.setText(document.get("education").toString());
            tvPastAttendant.setText(document.get("experience").toString());
        }
    }
}