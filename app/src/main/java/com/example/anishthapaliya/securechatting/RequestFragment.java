package com.example.anishthapaliya.securechatting;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    private RecyclerView mRequestList;

    private View mMainView;

    private DatabaseReference mFriendReqRef;
    private DatabaseReference mFriendRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private DatabaseReference mFriendRequestRef;

    String onlineUserId;

    public RequestFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView=inflater.inflate(R.layout.fragment_request, container, false);

        mRequestList=(RecyclerView)mMainView.findViewById(R.id.request_list);

        mAuth=FirebaseAuth.getInstance();
        onlineUserId=mAuth.getCurrentUser().getUid();

        mFriendReqRef= FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendRequestRef=FirebaseDatabase.getInstance().getReference().child("Friend_Req").child(onlineUserId);
        mUserRef=FirebaseDatabase.getInstance().getReference().child("users");
        mFriendRef=FirebaseDatabase.getInstance().getReference().child("Friends");

        mRequestList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRequestList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }




    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests,RequestViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Requests, RequestViewHolder>
                (
                        Requests.class,
                        R.layout.friend_request_all_users_layout,
                        RequestFragment.RequestViewHolder.class,
                        mFriendRequestRef

                ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position)
            {
                //Retrieving FriendReq node Data here
                final String list_users_id=getRef(position).getKey();

                final DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Toast.makeText(getContext(),"babe",Toast.LENGTH_LONG).show();

                                String requestType=dataSnapshot.getValue().toString();

                                if(requestType.equals("received")){

                                    mUserRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final String userName=dataSnapshot.child("name").getValue().toString();
                                            final String image=dataSnapshot.child("image").getValue().toString();
                                            final String status=dataSnapshot.child("status").getValue().toString();

                                            viewHolder.setUserName(userName);
                                            viewHolder.setImage(image,getContext());
                                            viewHolder.setStatus(status);

                                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    CharSequence options[]=new CharSequence[]{
                                                            "Accept Friend Request",
                                                            "Cancel Friend Request"

                                                    };
                                                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                                    builder.setTitle("Friend Req Options");
                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            //Click Event for eacg item
                                                            if(i==0){
                                                                final String currentDate= DateFormat.getDateTimeInstance().format(new Date());



                                                                mFriendRef.child(onlineUserId).child(list_users_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mFriendRef.child(list_users_id).child(onlineUserId).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mFriendReqRef.child(onlineUserId).child(list_users_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                mFriendReqRef.child(list_users_id).child(onlineUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                Toast.makeText(getContext(),"Friend Req Accepted Sucessfully...",Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });


                                                            }
                                                            if(i==1){
                                                                mFriendReqRef.child(onlineUserId).child(list_users_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mFriendReqRef.child(list_users_id).child(onlineUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                Toast.makeText(getContext(),"Friend Req Cancelled Successfully",Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                            }

                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });
                                        }


                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                }

                                else if(requestType.equals("sent")){


                                    Button req_send_btn=viewHolder.mView.findViewById(R.id.request_accept_btn);
                                    req_send_btn.setText("Req Sent");

                                    viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                    mUserRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final String userName=dataSnapshot.child("name").getValue().toString();
                                            final String image=dataSnapshot.child("name").getValue().toString();
                                            final String status=dataSnapshot.child("status").getValue().toString();

                                            viewHolder.setUserName(userName);
                                            viewHolder.setImage(image,getContext());
                                            viewHolder.setStatus(status);


                                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    CharSequence options[]=new CharSequence[]{
                                                            "Cancel Friend Request"

                                                    };
                                                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                                    builder.setTitle(" Friend Request Sent");
                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            //Click Event for eacg item

                                                            if(i==0){
                                                                mFriendReqRef.child(onlineUserId).child(list_users_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mFriendReqRef.child(list_users_id).child(onlineUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                Toast.makeText(getContext(),"Friend Req Cancelled Successfully",Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                            }

                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }
        };
        mRequestList.setAdapter(firebaseRecyclerAdapter);
    }






    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
            View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setUserName(String userName) {

            TextView userNameDisplay=(TextView)mView.findViewById(R.id.request_profile_name);
            userNameDisplay.setText(userName);
        }

        public void setImage(final String image, final Context ctx) {

            final CircleImageView imagedisp=(CircleImageView) mView.findViewById(R.id.request_profile_image);

            Picasso.with(ctx).load(String.valueOf(image)).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_png)
                    .into(imagedisp, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(image).placeholder(R.drawable.avatar_png);

                        }
                    });
        }

        public void setStatus(String status) {

            TextView statusDisp=(TextView) mView.findViewById(R.id.request_profile_status);
            statusDisp.setText(status);
        }
    }

}
