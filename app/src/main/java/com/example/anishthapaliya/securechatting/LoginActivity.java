package com.example.anishthapaliya.securechatting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginBtn;
    private ProgressDialog mLoginProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        loginEmail=(EditText) findViewById(R.id.login_email);
        loginPassword=(EditText) findViewById(R.id.login_password);
        loginBtn=(Button) findViewById(R.id.login_button);
        mLoginProgress=new ProgressDialog(this);
        //For Toolbar
        mToolbar=(Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Login");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=loginEmail.getText().toString();
                String password=loginPassword.getText().toString();

                if(!(TextUtils.isEmpty(email))||!(TextUtils.isEmpty(password))){

                    mLoginProgress.setTitle("Logging in!!");
                    mLoginProgress.setMessage("please wait while we check your credentials......");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email,password);

                }

            }
        });
    }

    public void loginUser(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    mLoginProgress.dismiss();
                    Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }

                else{

                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot signin,Please check the form and try again.....",Toast.LENGTH_LONG).show();

                }

            }
        });
    }
}
