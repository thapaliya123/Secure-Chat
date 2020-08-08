package com.example.anishthapaliya.securechatting;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;
    DatabaseReference mUserDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        //For Toolbar
        mToolbar=findViewById(R.id.main_page_toolbar);
        //error occured?
        //change import to android.support.v7.widget.Toolbar;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Secure Chat");

        mSectionPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager=(ViewPager) findViewById(R.id.main_tab_pager);
        mViewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout=(TabLayout) findViewById(R.id.main_tab);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        FirebaseUser currentUser=mAuth.getCurrentUser();
//        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
    }
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {

            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f=null;

        //    if(position==0){
              //  f=new RequestFragment();
          //  }

            if(position==0){
                f=new ChatFragment();
            }

            if(position==1){
                f=new FriendFragment();
            }
            return f;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
            //    case 0:
                 //   return "REQUESTS";

                case 0:
                    return "CHAT";

                case 1:
                    return "FRIEND";

                default:
                    return null;
            }
        }
    }


    public void onStart(){
            super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser==null){

            sendToStart();
        }

    //    else{
      //      mUserDatabase.child("online").setValue(true);
        //}
        if(currentUser!=null){
            String onlineUserId=mAuth.getCurrentUser().getUid();
            mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(onlineUserId);
            mUserDatabase.child("online").setValue("true");
        }
    }

    //This method means when main activity is not used
  public void onStop() {
       super.onStop();
       FirebaseUser currentUser=mAuth.getCurrentUser();
       if (currentUser != null) {
           String onlineUserId=mAuth.getCurrentUser().getUid();
           mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(onlineUserId);
           mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
       }
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

         getMenuInflater().inflate(R.menu.main_menu,menu);

         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()==R.id.main_logout_button){
             mAuth=FirebaseAuth.getInstance();
             FirebaseUser mCurrentUser=mAuth.getCurrentUser();

             if(mCurrentUser!=null){
                 String onlineUserId=mCurrentUser.getUid();
                 mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(onlineUserId);
                 mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
             }

             FirebaseAuth.getInstance().signOut();
             sendToStart();
         }
    if(item.getItemId()==R.id.main_settings_button){
             Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
             startActivity(settingsIntent);
    }

    if(item.getItemId()==R.id.main_user_button){

             Intent userIntent=new Intent(MainActivity.this,UsersActivity.class);
             startActivity(userIntent);
    }
         return true;
    }
    public void sendToStart(){
        Intent welcomeIntent=new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }
}
