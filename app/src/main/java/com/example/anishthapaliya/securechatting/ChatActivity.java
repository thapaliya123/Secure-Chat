package com.example.anishthapaliya.securechatting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private DatabaseReference loadMessageReference;

    private TextView mTextView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList=new ArrayList<>();

    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int GALLERY_PICK=1;

    //STORAGE FIREBASE
    private StorageReference mImageStorage;

    private  static  final int TOTAL_ITEMS_TO_LOAD=8;
    private int mCurrentPage=1;

    //New solution
    private int itemPosition=0;

    private String mLastKey="";
    private String mPrevKey="";

    private ProgressDialog loadingBar;


    @Override

    /*------TRY TO RETRIEVE MAN NAME--------*/

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);

        loadingBar=new ProgressDialog(this);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar actionBar = getSupportActionBar();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        //Adding offline feature

        mRootRef.keepSynced(true);


        loadMessageReference=FirebaseDatabase.getInstance().getReference();
        loadMessageReference.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //Custom Bar Items
        mTextView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter=new MessageAdapter(messagesList);

        mMessagesList=(RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout=new LinearLayoutManager(this);
        mRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        mChatUser = getIntent().getStringExtra("user_id");

        //For storage
        mImageStorage= FirebaseStorage.getInstance().getReference();

        LoadMessages();




        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image=dataSnapshot.child("thumb_image").getValue().toString();
                mTextView.setText(name);

                Picasso.with(ChatActivity.this).load(String.valueOf(image)).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_png).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });

                final String online=dataSnapshot.child("online").getValue().toString();

                if(online.equals("true")){
                 mLastSeenView.setText("online");
                }
                else{
                    SecureChat getTIme=new SecureChat();
                    long lastSeen=Long.parseLong(online);
                    String lastSeenDisplayTime=getTIme.getTimeAgo(lastSeen,getApplicationContext()).toString();

                    mLastSeenView.setText(lastSeenDisplayTime);
                }



            }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!(dataSnapshot.hasChild(mChatUser))) {
                    Map chatAddMap = new HashMap();

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                itemPosition=0;

                LoadMoreMessages();
            }
        });


    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK&&data!=null){
            loadingBar.setTitle("Sending Chat Image");
            loadingBar.setMessage("Please wait while your chat msg is sending.....");
            loadingBar.show();

            Uri imageUri=data.getData();

            final String currentUserRef="messages/"+mCurrentUserId+"/"+mChatUser;
            final String chatUserRef="messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference userMessagePush=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            final String pushId=userMessagePush.getKey();

            StorageReference filePath=mImageStorage.child("message_images").child(pushId+".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
               if(task.isSuccessful()){

                   final String downloadUri=task.getResult().getDownloadUrl().toString();

                   Map messageMap=new HashMap();
                   messageMap.put("message",downloadUri);
             //      messageMap.put("seen",false);
                   messageMap.put("type","image");
                   messageMap.put("from",mCurrentUserId);

                   Map messageUserMap=new HashMap();
                   messageUserMap.put(currentUserRef+"/"+pushId,messageMap);
                   messageUserMap.put(chatUserRef+"/"+pushId,messageMap);

                   mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if(databaseError!=null){
                                       //To show which type of error we got
                                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                    }

                                    mChatMessageView.setText("");

                                    loadingBar.dismiss();
                       }

                   });

                   Toast.makeText(ChatActivity.this,"Picture Sent Succesfully..",Toast.LENGTH_LONG).show();
                   loadingBar.dismiss();
               }
               else{
                   Toast.makeText(ChatActivity.this, "Pic not sent..Try Again", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
               }
                }
            });
        }
    }

    private void LoadMoreMessages(){
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);//To show 8 msg in one page

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Messages message=dataSnapshot.getValue(Messages.class);
                        String messageKey=dataSnapshot.getKey();

                messagesList.add(itemPosition++,message);



                if(!mPrevKey.equals(messageKey)){
                        messagesList.add(itemPosition++,message);
                }

                else{
                    mPrevKey=mLastKey;
                }

                if(itemPosition==1){

                            mLastKey=messageKey;
                        }



                        Log.d("TOTALKEYS","|Last Key:"+mLastKey+"|Prev Key:"+mPrevKey+"|Message Key:"+messageKey);


                        mAdapter.notifyDataSetChanged();

                        mRefreshLayout.setRefreshing(false);

                        mLinearLayout.scrollToPositionWithOffset(8,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void LoadMessages(){
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        messageRef.keepSynced(true);

        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);//To show 8 msg in one page

        messageQuery.addChildEventListener(new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages=dataSnapshot.getValue(Messages.class);
                itemPosition++;

                if(itemPosition==1){
                    String messageKey=dataSnapshot.getKey();

                    mLastKey=messageKey;
                    mPrevKey=messageKey;
                }


                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1);//To go to bottom of message list
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void sendMessage() throws Exception {

        String message=mChatMessageView.getText().toString();
     /*   Rsa.readWriteKEY();
     PrivateKey privateKey=Rsa.getPrivateKey();
        System.out.println(privateKey);
     byte[] encryptedMsgInNum=Rsa.encrypt(privateKey,message);*/
     String encryptedMsg=Aes.encrypt(message);


        System.out.println(encryptedMsg);


        if(!(TextUtils.isEmpty(encryptedMsg))){

            String current_user_ref="messages/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_ref="messages/"+mChatUser+"/"+mCurrentUserId;
            DatabaseReference userMessagePush=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id=userMessagePush.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",encryptedMsg);
          //  messageMap.put("seen",false);
            messageMap.put("type","text");
            //messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);
            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError !=null){
                            Log.d("CHAT_LOG",databaseError.getMessage().toString());
                        }
                }
            });
        }

    }



 public void onStart(){
        super.onStart();

        String cId=mAuth.getCurrentUser().getUid();

        mRootRef.child("users").child(cId).child("online").setValue("true");


 }

 public void onStop(){
        super.onStop();

        String cId=mAuth.getCurrentUser().getUid();
        mRootRef.child("users").child(cId).child("online").setValue(ServerValue.TIMESTAMP);
 }



}
