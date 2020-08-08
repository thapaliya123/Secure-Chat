package com.example.anishthapaliya.securechatting;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private byte [] originalMessageInNum;

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessageList){

        this.mMessageList=mMessageList;
    }

    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public ImageView messageImage;
        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText=(TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage= (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            messageImage=(ImageView) itemView.findViewById(R.id.message_image_layout);
        }
    }

    @Override


    public void onBindViewHolder(final MessageViewHolder viewHolder, int i){
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();


        Messages c=mMessageList.get(i);

        String message=c.getMessage();


       // byte [] messageInNum=message.getBytes();

      /*  try {
            PublicKey publicKey=Rsa.getPublicKey();

            originalMessageInNum=Rsa.decrypt(publicKey,messageInNum);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        String originalMessage= null;
        try {
            originalMessage = Aes.decrypt(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewHolder.messageText.setText(originalMessage);

        String fromUser=c.getFrom();
        String message_type=c.getType();

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(fromUser);
        //Adding Firebase Offline Feature
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
           //    String name=dataSnapshot.child("name").getValue().toString();
                String image=dataSnapshot.child("thumb_image").getValue().toString();

           Picasso.with(viewHolder.profileImage.getContext()).load(image).placeholder(R.drawable.avatar_png).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        if(message_type.equals("text")){
            viewHolder.messageImage.setVisibility(View.INVISIBLE);


            if(fromUser.equals(currentUserId)){
                viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background_color);

                viewHolder.messageText.setTextColor(Color.WHITE);

               viewHolder.messageText.setGravity(Gravity.RIGHT);//To display in right side
            }
            else{
                viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);

                viewHolder.messageText.setTextColor(Color.WHITE);

                viewHolder.messageText.setGravity(Gravity.LEFT);

            }
            viewHolder.messageText.setText(originalMessage);


        }

        else{
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setPadding(0,0,0,0);

            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage()).
                    placeholder(R.drawable.avatar_png).into(viewHolder.messageImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
