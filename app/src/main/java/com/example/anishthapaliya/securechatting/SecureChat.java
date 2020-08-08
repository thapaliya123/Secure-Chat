package com.example.anishthapaliya.securechatting;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class SecureChat extends Application {
    public DatabaseReference mUserDatabase;
    public FirebaseAuth mAuth;


    //For last Seen Feature
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public void onCreate() {

        super.onCreate();

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser mCurrentUser=mAuth.getCurrentUser();

       FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        if(mCurrentUser!=null){
            String currentUserId=mAuth.getCurrentUser().getUid();

            mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
    //*****For last Seen Feature

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS)

        {
            return "active few seconds ago";
        }

        else if (diff < 2 * MINUTE_MILLIS)

        {
            return "a minute ago";
        }

        else if (diff < 50 * MINUTE_MILLIS)
        {
            return diff / MINUTE_MILLIS + " minutes ago";
        }

        else if (diff < 90 * MINUTE_MILLIS)
        {
            return "an hour ago";
        }

        else if (diff < 24 * HOUR_MILLIS)
        {
            return diff / HOUR_MILLIS + " hours ago";
        }

        else if (diff < 48 * HOUR_MILLIS)
        {
            return "yesterday";
        }
        else
        {
            return diff / DAY_MILLIS + " days ago";
        }
    }

}
