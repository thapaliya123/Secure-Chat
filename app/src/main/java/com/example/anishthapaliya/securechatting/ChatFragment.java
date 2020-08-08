package com.example.anishthapaliya.securechatting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.ValueIterator;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment
{
    private View myMainView;

    private RecyclerView myChatList;
    private DatabaseReference friendRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    String onlineUserId;

    public ChatFragment(){
        //Require Empty Constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        myMainView=inflater.inflate(R.layout.fragment_chat, container, false);

        myChatList=(RecyclerView) myMainView.findViewById(R.id.chat_list);

        mAuth=FirebaseAuth.getInstance();
        onlineUserId=mAuth.getCurrentUser().getUid();//To get current user id

        friendRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);

        //Offline Feature
        friendRef.keepSynced(true);

        userRef=FirebaseDatabase.getInstance().getReference().child("users");
        //Offline Feature

        userRef.keepSynced(true);

        myChatList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatList.setLayoutManager(linearLayoutManager);

        return myMainView;
    }

    @Override
    public void onStart()  {
        super.onStart();
        FirebaseRecyclerAdapter<Chats,ChatFragment.ChatViewHolder>
                firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Chats,ChatViewHolder>(
                Chats.class,
                R.layout.users_single_layout,
                ChatFragment.ChatViewHolder.class,
                friendRef
        )
        {

            @Override
            protected void populateViewHolder(final ChatFragment.ChatViewHolder viewHolder, Chats model, int position) {

                final String list_user_id=getRef(position).getKey();
                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        String userName=dataSnapshot.child("name").getValue().toString();
                        String image=dataSnapshot.child("thumb_image").getValue().toString();

                        String userStatus=dataSnapshot.child("status").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String userOnline=(String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }

                        viewHolder.setName(userName);
                        viewHolder.setUserStatus(userStatus);
                        viewHolder.setImage(image,getContext());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (dataSnapshot.child("online").exists()) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", list_user_id);
                                    startActivity(chatIntent);
                                }
                                else
                                {
                                    userRef.child(list_user_id).child("online").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myChatList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public ChatViewHolder(View itemView) {
            super(itemView);

            mView=itemView;


        }

        public void setUserStatus(String userStatus)
        {
            TextView user_status=(TextView) mView.findViewById(R.id.user_single_status);
            user_status.setText(userStatus);
        }

        public void setName(String userName)
        {
            TextView user_name=(TextView) mView.findViewById(R.id.user_single_name);
            user_name.setText(userName);
        }

        public void setUserOnline(String userOnline) {
            ImageView user_online=(ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(userOnline.equals("true")){
                user_online.setVisibility(View.VISIBLE);
            }

            else
                {
                    user_online.setVisibility(View.INVISIBLE);
            }
        }

        public void setImage(String image, Context context) {
            CircleImageView chatImage=(CircleImageView) mView.findViewById(R.id.user_single_image);

            Picasso.with(context).load(String.valueOf(image)).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_png).into(chatImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                }
            });

        }
    }
}
