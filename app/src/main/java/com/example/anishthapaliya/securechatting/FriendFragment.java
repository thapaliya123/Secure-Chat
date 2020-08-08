package com.example.anishthapaliya.securechatting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendFragment extends Fragment {
    private RecyclerView mFriendList;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;


    private View mMainView;

    public FriendFragment(){
        //Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


                mMainView=inflater.inflate(R.layout.fragment_friend,container,false);

                mFriendList=(RecyclerView) mMainView.findViewById(R.id.friend_list);
                mAuth=FirebaseAuth.getInstance();

                mCurrentUserId=mAuth.getCurrentUser().getUid();

                mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
                //offline feature
                mFriendDatabase.keepSynced(true);
                mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users");
                //adding firebase offline feature
                mUserDatabase.keepSynced(true);

                mFriendList.setHasFixedSize(true);
                mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));



        // Inflate the layout for this fragment
        return  mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend,FriendViewHolder> friendRecyclerAdapter=new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(
             Friend.class,
             R.layout.users_single_layout,
             FriendViewHolder.class,
             mFriendDatabase

        ) {

            @Override
            protected void populateViewHolder(final FriendViewHolder viewHolder, Friend model, int position) {
                viewHolder.setDate(model.getDate());

                final String list_user_id=getRef(position).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName=dataSnapshot.child("name").getValue().toString();
                        String image=dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String userOnline=(String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }

                        viewHolder.setName(userName);
                        viewHolder.setImage(image,getContext());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[]=new CharSequence[]{"open profile","send message"};
                                 AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                builder.setTitle("select options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                            //Click Event for eacg item
                                        if(i==0){
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);

                                        }
                                        if(i==1){
                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            startActivity(chatIntent);
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
        };
        mFriendList.setAdapter(friendRecyclerAdapter);
    }
    public static class FriendViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FriendViewHolder(View itemView){
            super(itemView);

            mView=itemView;

        }

        public void setDate(String date) {
            TextView userNameView=(TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText("Friends Since: \n"+date);
        }

        public void setName(String name){
            TextView userNameView=(TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setImage(String image,final Context ctx) {
                    CircleImageView imageView=(CircleImageView) mView.findViewById(R.id.user_single_image);

                    Picasso.with(ctx).load(String.valueOf(image)).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_png).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });

        }


        public void setUserOnline(String online_status){
            ImageView userOnlineView=(ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);

            }
            else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }


    }
}
