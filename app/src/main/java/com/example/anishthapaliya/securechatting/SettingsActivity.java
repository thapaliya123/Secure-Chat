package com.example.anishthapaliya.securechatting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
        private DatabaseReference mUserDatabase;
        private FirebaseUser currentUser;

        //Android Layout

        private CircleImageView mCircleImage;
        private TextView mTextDisplay;
        private TextView mTextStatus;
        private Button mChangeStatusBtn;
        private Button mChangeImageBtn;

        private static final int GALLERY_PICK=1;

        //creating firebase database storage references
        private StorageReference mStorage;

        //Progress Dialogue

        private ProgressDialog mProgressDialogue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mTextDisplay=(TextView) findViewById(R.id.settings_display_name);
        mTextStatus=(TextView) findViewById(R.id.settings_status);
        mCircleImage=(CircleImageView) findViewById(R.id.settings_image);
        mChangeStatusBtn=(Button) findViewById(R.id.settings_status_btn);
        mChangeImageBtn=(Button) findViewById(R.id.settings_image_btn);



        //For storage
        mStorage= FirebaseStorage.getInstance().getReference();

        currentUser= FirebaseAuth.getInstance().getCurrentUser();

        String currentUid=currentUser.getUid();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);
        //To add firebase offline capabilities
        mUserDatabase.keepSynced(true);

        //To take all data from realtime database

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                mTextDisplay.setText(name);
                mTextStatus.setText(status);

                //for displaying image
                //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar_png).into(mCircleImage);
                if (!image.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_png).into(mCircleImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(thumbImage).placeholder(R.drawable.avatar_png).into(mCircleImage);


                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String statusValue=mTextStatus.getText().toString();
                Intent statusIntent=new Intent(SettingsActivity.this,StatusActivity.class);

                //To send text value in status activity
                statusIntent.putExtra("status_value",statusValue);
                startActivity(statusIntent);
            }
        });

        //For image
        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //for choosing image from gallery
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

              /*  CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
*/
            }

        });
    }

    //To get Crop result create OncreateActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
  if(requestCode==GALLERY_PICK&&resultCode==RESULT_OK){
      Uri imageUri=data.getData();
      CropImage.activity(imageUri).setAspectRatio(1,1).start(this);
  }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialogue = new ProgressDialog(SettingsActivity.this);
                mProgressDialogue.setTitle("Pic Uploading!!!!");
                mProgressDialogue.setMessage("Please wait while we are uploading picture for you.........");
                mProgressDialogue.setCanceledOnTouchOutside(false);
                mProgressDialogue.show();

                Uri resultUri = result.getUri();

                Bitmap thumbBitmap = null;

                String current_user_id = currentUser.getUid();
                File thumbFile = new File(resultUri.getPath());
                try {
                    thumbBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).setQuality(75).compressToBitmap(thumbFile);
                    //Compressing Size and decreasing quality
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] data1 = baos.toByteArray();


                final StorageReference filepath = mStorage.child("profile_image").child(current_user_id + ".jpg");
                final StorageReference thumbFilePath = mStorage.child("profile_image").child("thumbs").child(current_user_id + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = filepath.putBytes(data1);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()) {

                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image", download_url);
                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    mProgressDialogue.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Success Uploading.", Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        });


                                    } else {

                                        Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                        mProgressDialogue.dismiss();

                                    }


                                }
                            });


                        } else {

                            Toast.makeText(SettingsActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                            mProgressDialogue.dismiss();

                        }

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }


    }


    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}