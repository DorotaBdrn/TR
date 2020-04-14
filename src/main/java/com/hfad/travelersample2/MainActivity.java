package com.hfad.travelersample2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;//zmienilam z recycle View
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postRef, LikesRef, TripsRef;
    private CircleImageView navProfileImage;
    private TextView naProfileUserName;
    String currentUserId;
    Boolean LikeChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        setSupportActionBar((Toolbar) findViewById(R.id.main_page_toolbar));
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            getSupportActionBar().setTitle("Home");
        }

        ImageButton addNewPostButton = findViewById(R.id.add_new_post_button);

        DrawerLayout drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        postList = findViewById(R.id.all_users_post_list);
//        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = navView.findViewById(R.id.nev_profile_image);
        naProfileUserName = navView.findViewById(R.id.nev_full_name);


        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        naProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUsertoPostActivity();
            }
        });

        DisplayAllUserPosts();
    }

    @Override
    protected void onStart() {

        super.onStart();
        DisplayAllUserPosts();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToLoginActivity();

        } else {
            CheckUserExistence();
        }
    }

    private void DisplayAllUserPosts() {


        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>().
                setQuery(postRef, Post.class).build();
        FirebaseRecyclerAdapter<Post, PostViewHolder> adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

//         adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {


                final String PostKey = getRef(position).getKey();// pobranie keyid kiedy klikamy na post

                holder.username.setText(model.getFullname());
                holder.time.setText(String.format("%s", model.getTime()));
                holder.date.setText(String.format("%s", model.getDate()));
                holder.description.setText(model.getDescription());
                holder.destination.setText(model.getDestination());

                holder.setLikeButtonStatus(PostKey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);//pobranie PostKey przez clickPostActivity
                        startActivity(clickPostIntent);


                    }
                });

                holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commnetsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commnetsIntent.putExtra("PostKey", PostKey);//pobranie PostKey przez clickPostActivity
                        startActivity(commnetsIntent);
                    }
                });

                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (LikeChecker.equals(true)) {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                                        LikesRef.child(PostKey).child(currentUserId).removeValue();
                                        LikeChecker = false;
                                    } else {
                                        LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                        LikeChecker = false;

                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                Picasso.get().load(model.getProfileimage()).into(holder.image);
                Picasso.get().load(model.getPostimage()).into(holder.postImage);
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout, parent, false);
                return new PostViewHolder(view);
            }
        };

        postList.setAdapter(adapter);
        adapter.startListening();

    }

    private void SendUsertoPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void CheckUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(current_user_id)) {

                    SendUserToSetUpActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetUpActivity() {

        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LogInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_new_post:
                SendUsertoPostActivity();
                break;
            case R.id.profile:
                SendUserToProfileActivity();
                Toast.makeText(this, "Profile", Toast.LENGTH_LONG).show();
                break;
            case R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_LONG).show();
                break;
            case R.id.search:
                SendUserToFindFriendsActivity();
                Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();
                break;
            case R.id.searchPlaces:
                SendUserToFindPlaceActivity();
                Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();
                break;
            case R.id.camera:
                Toast.makeText(this, "Camera", Toast.LENGTH_LONG).show();
                break;
            case R.id.laiks:
                Toast.makeText(this, "Likes", Toast.LENGTH_LONG).show();
                break;
            case R.id.settings:

                SendUserToSettingActivity();
                break;
            case R.id.log_out:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

        }
    }

    private void SendUserToFindPlaceActivity() {
        Intent settingIntent = new Intent(MainActivity.this, FindPlaceActivity.class);
        startActivity(settingIntent);
    }


    private void SendUserToSettingActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToProfileActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(settingsIntent);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView username, date, time, description, destination;
        CircleImageView image;
        ImageView postImage;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikesButton;
        int coutLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostViewHolder(View mView) {
            super(mView);

            username = mView.findViewById(R.id.post_user_name);
            date = mView.findViewById(R.id.post_date);
            time = mView.findViewById(R.id.post_time);
            description = mView.findViewById(R.id.post_description);
            destination = mView.findViewById((R.id.post_destination));
            image = mView.findViewById(R.id.post_profile_image);
            postImage = mView.findViewById(R.id.post_image);

            likePostButton = mView.findViewById(R.id.like_button);
            commentPostButton = mView.findViewById(R.id.comment_button);
            displayNoOfLikesButton = mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), TripsActivity.class));
                }
            });
        }


        public void setLikeButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                        coutLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.likek_red_24dp);
                        displayNoOfLikesButton.setText(Integer.toString(coutLikes) + (" Likes"));
                    } else {
                        coutLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.ic_like_black_24dp);
                        displayNoOfLikesButton.setText(Integer.toString(coutLikes));


                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
