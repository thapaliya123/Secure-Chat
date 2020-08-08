package com.example.anishthapaliya.securechatting;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    //Android Layout
    private Toolbar mToolbar;
    private EditText mstatus;
    private Button mSaveChangesBtn;

    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //ProgressDialogue
    private ProgressDialog mProgressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mstatus=(EditText) findViewById(R.id.status_input);
        mSaveChangesBtn=(Button) findViewById(R.id.status_save_btn);


        mToolbar=(Toolbar) findViewById(R.id.settings1_status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //To take text from Settings activity
        String statusValue=getIntent().getStringExtra("status_value");
        mstatus.setText(statusValue);
        //Firebase
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=mCurrentUser.getUid();
        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);

        mSaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialogue=new ProgressDialog(StatusActivity.this);

                mProgressDialogue.setTitle("Saving Changing......");
                mProgressDialogue.setMessage("Please wait while we are saving the changes!!!!!!");
                mProgressDialogue.show();

                String status=mstatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialogue.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"There was some error in saving context",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        }
}
