package com.example.sarthi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpAttendant extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY =2 ;
    private static final int PICK_IMAGE_CAMERA = 1;
    private static final int PICK_IMAGE_GALLERY =2 ;
    EditText attendant_first_name,attendant_last_name,attendant_email,attendant_age,attendant_education;
    Button btnSaveAttendant,btnExp;
    RadioGroup radioGender,radioExp;
    RadioButton radioButton,radioYes,radioNo;
    TextView tvExperience;
    int selected;
    StorageReference storageReference;
    StorageTask storageTask;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    Map<String,Object> attendants=new HashMap<>();
    FirebaseUser attendant=fauth.getCurrentUser();
    String isExp,url;
    LinearLayout layout;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_attendant);
        attendant_first_name=findViewById(R.id.attendant_first_name);
        attendant_last_name=findViewById(R.id.attendant_last_name);
        attendant_email=findViewById(R.id.attendant_email);
        attendant_age=findViewById(R.id.attendant_age);
        attendant_education=findViewById(R.id.attendant_education);
        btnSaveAttendant=findViewById(R.id.btnSaveAttendant);
        radioGender=findViewById(R.id.radioGender);
        radioExp=findViewById(R.id.radioExp);
        radioYes=findViewById(R.id.radioYes);
        radioNo=findViewById(R.id.radioNo);
        layout=findViewById(R.id.layout);
        btnExp=findViewById(R.id.btnExp);
        pb=findViewById(R.id.pb);
        tvExperience=findViewById(R.id.tvExperience);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        radioYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExp=radioYes.getText().toString();
                layout.setVisibility(View.VISIBLE);

            }
        });
        radioNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExp=radioNo.getText().toString();
                url=null;
                layout.setVisibility(View.GONE);
            }
        });
        btnExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });
        btnSaveAttendant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected=radioGender.getCheckedRadioButtonId();
                radioButton=findViewById(selected);
                if(checkValidations()) {
                    attendants.put("name",attendant_first_name.getText().toString()+" "+attendant_last_name.getText().toString());
                    attendants.put("age",attendant_age.getText().toString());
                    attendants.put("email",attendant_email.getText().toString());
                    attendants.put("gender",radioButton.getText().toString());
                    attendants.put("education",attendant_education.getText().toString());
                    attendants.put("type","attendant");
                    attendants.put("phone",attendant.getPhoneNumber());
                    attendants.put("experience",isExp);
                    attendants.put("url",url);
                    db.collection("data").document(attendant.getUid()).set(attendants).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(SignUpAttendant.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(SignUpAttendant.this,Attendant_Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        }
                    });
                }
            }
        });
    }
    private void dialog() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SignUpAttendant.this,
                                new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
                    } else {
                        openCamera();
                    }
                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SignUpAttendant.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
                    } else {
                        selectImage();
                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_GALLERY && grantResults.length>0)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                selectImage();
            }
            else
            {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==REQUEST_CODE_CAMERA && grantResults.length>0)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }
            else
            {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(intent,PICK_IMAGE_CAMERA);
        }
    }

    private void selectImage() {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(intent,PICK_IMAGE_GALLERY);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_GALLERY && resultCode==RESULT_OK)
        {
            if(data!=null) {
                Uri selectedImageUri = data.getData();
                uploadImage(selectedImageUri);
            }

        }
        if(requestCode==PICK_IMAGE_CAMERA && resultCode==RESULT_OK)
        {
            if(data!=null)
            {
                Bitmap capturedImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                assert capturedImage != null;
                Uri capturedImageUri=getImageUri(this,capturedImage);
                uploadImage(capturedImageUri);
            }
        }
    }
    private void uploadImage(Uri image_uri) {
        if (image_uri==null){
            Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
        }
        else {
            final StorageReference reference= storageReference.child(System.currentTimeMillis()+"."+getFileExtension(image_uri));
            storageTask= reference.putFile(image_uri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    url=uri.toString();
                                    pb.setVisibility(View.GONE);
                                    Drawable img = tvExperience.getContext().getResources().getDrawable( R.drawable.ic_uploaded );
                                    tvExperience.setCompoundDrawablesWithIntrinsicBounds(null,null,img,null);
                                    Toast.makeText(SignUpAttendant.this, "Successful", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpAttendant.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            pb.setVisibility(View.GONE);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            pb.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private boolean checkValidations() {
        if(TextUtils.isEmpty(attendant_first_name.getText().toString())){
            Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(attendant_email.getText().toString())){
            Toast.makeText(this, "Please enter an email Address", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(attendant_age.getText().toString())){
            Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(attendant_education.getText().toString())){
            Toast.makeText(this, "Please enter your educational qualification", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(isExp.isEmpty()){
            Toast.makeText(this, "Please choose either yes or no", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(selected==-1)
        {
            Toast.makeText(this, "Please choose gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}