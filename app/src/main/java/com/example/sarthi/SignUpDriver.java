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
import android.widget.ProgressBar;
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

public class SignUpDriver extends AppCompatActivity {
    private static final int REQUEST_CODE_GALLERY =2 ;
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int PICK_IMAGE_CAMERA = 1;
    private static final int PICK_IMAGE_GALLERY = 2;
    StorageReference storageReference;
    StorageTask storageTask;
    EditText driver_first_name,driver_last_name,driver_model,driver_car_number,driver_email,driver_radius;
    TextView tv;
    ProgressBar pb;
    Button btnSaveDriver,btnId,btnInsurance,btnRC,btnFront,btnBack;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    Map<String,Object> drivers=new HashMap<>();
    FirebaseUser driver=fauth.getCurrentUser();
    String urlId,urlInsurance,urlRC,urlFront,urlBack;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type="";
        setContentView(R.layout.activity_sign_up_driver);
        driver_first_name=findViewById(R.id.driver_first_name);
        driver_model=findViewById(R.id.driver_model);
        driver_car_number=findViewById(R.id.driver_car_number);
        driver_last_name=findViewById(R.id.driver_last_name);
        driver_email=findViewById(R.id.driver_email);
        driver_radius=findViewById(R.id.driver_radius);
        btnSaveDriver=findViewById(R.id.btnSaveDriver);
        btnId=findViewById(R.id.btnId);
        btnInsurance=findViewById(R.id.btnInsurance);
        btnRC=findViewById(R.id.btnRC);
        btnFront=findViewById(R.id.btnFront);
        btnBack=findViewById(R.id.btnBack);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        btnSaveDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidations()) {
                drivers.put("name",driver_first_name.getText().toString()+" "+driver_last_name.getText().toString());
                drivers.put("car number",driver_car_number.getText().toString());
                drivers.put("car model",driver_model.getText().toString());
                drivers.put("email",driver_email.getText().toString());
                drivers.put("urlId",urlId);
                drivers.put("urlInsurance",urlInsurance);
                drivers.put("urlRC",urlRC);
                drivers.put("urlFront",urlFront);
                drivers.put("urlBack",urlBack);
                drivers.put("type","driver");
                drivers.put("radius",driver_radius.getText().toString());
                drivers.put("phone",driver.getPhoneNumber());
                db.collection("data").document(driver.getUid()).set(drivers).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SignUpDriver.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(SignUpDriver.this,Driver_Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                });
            }
            }
        });
        btnId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="id";
                tv=findViewById(R.id.tvId);
                pb=findViewById(R.id.pbId);
                dialog();
            }
        });
        btnInsurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="insurance";
                tv=findViewById(R.id.tvInsurance);
                pb=findViewById(R.id.pbInsurance);
                dialog();
            }
        });
        btnRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="rc";
                tv=findViewById(R.id.tvRc);
                pb=findViewById(R.id.pbRC);
                dialog();
            }
        });
        btnFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="front";
                tv=findViewById(R.id.tvFront);
                pb=findViewById(R.id.pbFront);
                dialog();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="back";
                tv=findViewById(R.id.tvBack);
                pb=findViewById(R.id.pbBack);
                dialog();
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
                        ActivityCompat.requestPermissions(SignUpDriver.this,
                                new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
                    } else {
                        openCamera();
                    }
                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SignUpDriver.this,
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
                                    if(type.equals("id"))
                                        urlId=uri.toString();
                                    else if(type.equals("insurance"))
                                        urlInsurance=uri.toString();
                                    else if(type.equals("rc"))
                                        urlRC=uri.toString();
                                    else if(type.equals("front"))
                                        urlFront=uri.toString();
                                    else
                                        urlBack=uri.toString();
                                    pb.setVisibility(View.GONE);
                                    Drawable img = tv.getContext().getResources().getDrawable( R.drawable.ic_uploaded );
                                    tv.setCompoundDrawablesWithIntrinsicBounds(null,null,img,null);
                                    Toast.makeText(SignUpDriver.this, "Successful", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpDriver.this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if(TextUtils.isEmpty(driver_first_name.getText().toString())){
            Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(driver_email.getText().toString())){
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(driver_car_number.getText().toString())){
            Toast.makeText(this, "Please enter your car number", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(driver_model.getText().toString())){
            Toast.makeText(this, "Please enter model of your car", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(driver_radius.getText().toString())){
            Toast.makeText(this, "Please enter radius upto which you want to take a ride", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(TextUtils.isEmpty(urlId)||TextUtils.isEmpty(urlInsurance)||TextUtils.isEmpty(urlRC)
                ||TextUtils.isEmpty(urlFront)||TextUtils.isEmpty(urlBack))
        {
            Toast.makeText(this, "Please upload all images", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}