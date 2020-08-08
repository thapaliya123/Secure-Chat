package com.example.anishthapaliya.securechatting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;



public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private EditText registerDisplayName;
    private EditText registerEmail;
    private EditText registerPassword;
    private Button registerButton;
    public Toolbar mToolbar;

    DatabaseReference mDatabase;

    //progress bar
    private ProgressDialog mRegisterProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        registerDisplayName= findViewById(R.id.register_display_name);
        registerEmail= findViewById(R.id.login_email);
        registerPassword= findViewById(R.id.register_password);
        registerButton= findViewById(R.id.register_button);
        mToolbar= (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRegisterProgress=new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName=registerDisplayName.getText().toString();
                String email=registerEmail.getText().toString();
                String password=registerPassword.getText().toString();

               if(!(TextUtils.isEmpty(displayName))||!(TextUtils.isEmpty(email))||!(TextUtils.isEmpty(password))){
                   //for progress dialogue
                   mRegisterProgress.setTitle("Creating Account!!!");
                   mRegisterProgress.setMessage("Please wait,while we are creating accout for you......");
                   mRegisterProgress.setCanceledOnTouchOutside(false);
                   mRegisterProgress.show();

                   registerUser(displayName,email,password);

               }

               }

       });

    }

    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uId=current_user.getUid();
                    mDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(uId);
                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("name",displayName);
                    userMap.put("status","Hi,iam using secure chat app");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRegisterProgress.dismiss();
                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                            }
                        }
                    });



                }
                else{
                    mRegisterProgress.hide();
                    Toast.makeText(RegisterActivity.this,"Cannot sign in..Please check form and try again.....",Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}

