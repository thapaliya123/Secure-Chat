package com.example.anishthapaliya.securechatting;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

        private ImageView mProfileImage;
        private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
        private Button mProfileSendReqBtn;
        private Button mProfileDeclineBtn;
        private ProgressDialog mProgressDialogue;
        private String mcurrent_state;

        //Firebase
        private DatabaseReference mUserDatabase;
        private DatabaseReference mFriendRequestDatabase;
        private FirebaseUser mcurrentUser;
        private DatabaseReference mFriendDatabase;
        private DatabaseReference mNotificationDatabase;
        private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId=getIntent().getStringExtra("user_id");
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        mProfileImage=(ImageView) findViewById(R.id.profile_image);
        mProfileName=(TextView) findViewById(R.id.profile_display_name);
        mProfileStatus=(TextView) findViewById(R.id.profile_status);
        mProfileSendReqBtn=(Button) findViewById(R.id.profile_send_req_btn);
        mProfileDeclineBtn=(Button) findViewById(R.id.profile_decline_request_btn);

        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mcurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mcurrent_state="not_friends";

        //Once page load you should not see decline button i.e

        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineBtn.setEnabled(false);


        //progress Dialogue
        mProgressDialogue=new ProgressDialog(this);
        mProgressDialogue.setTitle("Loading User Data");
        mProgressDialogue.setMessage("please wait while we load user data");
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("thumb_image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar_png).into(mProfileImage);

                //-----------------Friend List/Request feature-----------------
                mFriendRequestDatabase.child(mcurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)) {
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mcurrent_state = "req_received";
                                mProfileSendReqBtn.setText("ACCEPT FRIEND REQUEST");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mcurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }
                            mProgressDialogue.dismiss();


                        } else {
                            mFriendDatabase.child(mcurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        mcurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this person");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialogue.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialogue.dismiss();
                                }

                            });

                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Once user click button the button should not work
               // mProfileSendReqBtn.setEnabled(false);


                //Not friends state
                if(mcurrent_state.equals("not_friends")){
                                    Map requestMap= new HashMap();
                                    requestMap.put(mcurrentUser.getUid()+"/"+userId+"/"+"request_type","sent");
                                    requestMap.put(userId+"/"+mcurrentUser.getUid()+"/"+"request_type","received");

                                    mFriendRequestDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            //For notification
                                        //    HashMap<String,String> notificationData=new HashMap<>();
                                          //  notificationData.put("from",mcurrentUser.getUid());
                                            //notificationData.put("type","request");
                                            //mNotificationDatabase.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            //    @Override
                                          //      public void onSuccess(Void aVoid) {

                                                  if(databaseError!=null){
                                                      Toast.makeText(ProfileActivity.this,"There are some errror in sending the request",Toast.LENGTH_LONG).show();
                                                  }
                                                    mcurrent_state="req_sent";
                                                    mProfileSendReqBtn.setText("CANCEL FREIND REQUEST");

                                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineBtn.setEnabled(false);
                                                    Toast.makeText(ProfileActivity.this,"Friend request sent successfully",Toast.LENGTH_LONG).show();

                                        //}
                                    //});
                            }

                    });
                }


                //Cancel Request
                 if(mcurrent_state.equals("req_sent")){
                    mFriendRequestDatabase.child(mcurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(userId).child(mcurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mcurrent_state="not_friends";
                                    mProfileSendReqBtn.setText("SEND FREIND REQUEST");

                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(true);
                                }
                            });
                        }
                    });

                }

                //REQ RECEIVED STATE

                if(mcurrent_state.equals("req_received")){
                        final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                       final Map friendMap=new HashMap();
                       final Map friendRequestMap=new HashMap();

                       friendMap.put(mcurrentUser.getUid()+"/"+userId+"/"+"date",currentDate);
                       friendMap.put(userId+"/"+mcurrentUser.getUid()+"/date",currentDate);

                       friendRequestMap.put(mcurrentUser.getUid()+"/"+userId+"/request_type",null);
                       friendRequestMap.put(userId+"/"+mcurrentUser.getUid()+"/request_type",null);

                       mFriendDatabase.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                           @Override
                           public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                               if(databaseError==null){
                                   mFriendRequestDatabase.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
                                       @Override
                                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                           if(databaseError==null){
                                               mProfileSendReqBtn.setEnabled(true);
                                               mcurrent_state="friends";
                                               mProfileSendReqBtn.setText("Unfriend this person");

                                               mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                               mProfileDeclineBtn.setEnabled(false);


                                           }

                                           else {
                                               String error=databaseError.getMessage();

                                               Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                                           }
                                       }
                                   });
                               }

                               else{
                                   String error=databaseError.getMessage();
                                   Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                               }
                           }
                       });

                }

                if(mcurrent_state.equals("friends")){

                     Map unFriendMap=new HashMap();
                     unFriendMap.put(mcurrentUser.getUid()+"/"+userId+"/date",null);
                     unFriendMap.put(userId+"/"+mcurrentUser.getUid()+"/date",null);

                     mFriendDatabase.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                         @Override
                         public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError==null){
                                    mProfileSendReqBtn.setEnabled(true);
                                    mcurrent_state="not_friends";
                                    mProfileSendReqBtn.setText("SEND FREIND REQUEST");

                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(false);

                                }
                                else{
                                    String error=databaseError.getMessage();
                                    Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                                }
                         }
                     });






                }

            }
        });
    }
}
