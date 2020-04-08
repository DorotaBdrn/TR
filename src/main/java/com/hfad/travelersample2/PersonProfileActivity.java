package com.hfad.travelersample2;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userCountry, userStatus, userGender, userDoB;
    private CircleImageView userProfileImage;
    private Button SendFriendRequestButton, CancelFriendRequest;


    private DatabaseReference profileUserRef, usersRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();


        //pobranie id uzytkownika na ktorego konto chcemy wejsc zeby np. wyslac zaproszenie
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        InitializeFields();

        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDoB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();


                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDoB.setText("Date of birth: " + myDoB);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }


        private void InitializeFields() {

            userName = findViewById(R.id.person_full_name);
            userProfName = findViewById(R.id.person_user_name);
            userStatus = findViewById(R.id.person_profile_status);
            userCountry = findViewById(R.id.person_country);
            userGender = findViewById(R.id.person_gender);
            userDoB = findViewById(R.id.person_dof);
            userProfileImage = findViewById(R.id.person_profile_pic);
            SendFriendRequestButton = findViewById(R.id.person_send_friend_request);
            CancelFriendRequest = findViewById(R.id.person_cancel_friend_request);

        }
    }

