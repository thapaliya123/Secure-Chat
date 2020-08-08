package com.example.anishthapaliya.securechatting;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUserDatabase;
    private EditText searchInputText;
    private ImageButton searchButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar=(Toolbar)findViewById(R.id.users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All User");

        searchInputText=(EditText) findViewById(R.id.search_input_text);
        searchButton=(ImageButton) findViewById(R.id.search_people_button);

        mUsersList=(RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));


        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users");

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchUserName = searchInputText.getText().toString();
                if (TextUtils.isEmpty(searchUserName)) {
                    Toast.makeText(UsersActivity.this, "Please write User name to search....", Toast.LENGTH_LONG).show();
                } else {
                    searchForPeopleAndFriends(searchUserName);
                }
            }
        });




    }

    private void searchForPeopleAndFriends(String searchUserName)
    {
        Toast.makeText(UsersActivity.this,"Searching",Toast.LENGTH_LONG).show();

        //To search using searchUserName
        Query searchPeopleAndFriends=mUserDatabase.orderByChild("name").startAt(searchUserName).endAt(searchUserName+"\uf8ff");

        FirebaseRecyclerAdapter<Users,UsersViewHolder>firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                searchPeopleAndFriends) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(model.getImage(),getApplicationContext());

                final String uId=getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",uId);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    //ViewHolder

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setName(String name){
            TextView mUserName=(TextView) mView.findViewById(R.id.user_single_name);
            mUserName.setText(name);


        }

        public void setStatus(String status){
            TextView mStatus=mView.findViewById(R.id.user_single_status);
            mStatus.setText(status);
        }

        public void setImage(String image, Context ctx) {

            CircleImageView userImageView=(CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(String.valueOf(image)).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_png).into(userImageView, new Callback() {
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
