package com.hfad.travelersample2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPlaceActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;
    private FirebaseRecyclerAdapter<Post, FindPlacesViewHolder> firebaseRecycleAdapter;

    private DatabaseReference allUsersDatabaseRef, allPostDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_place);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allPostDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = findViewById(R.id.find_place_appbar_layout);
        setSupportActionBar(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setTitle("Find Places");
        }

        SearchResultList = findViewById(R.id.search_result_list_places);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = findViewById(R.id.search_places_button);
        SearchInputText = findViewById(R.id.search_box_input_place);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = SearchInputText.getText().toString();

                SearchPlaces(searchBoxInput);
            }
        });

    }

    private void SearchPlaces(String searchBoxInput) {

        Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show();
        Query searchPlacesQuary = allPostDatabaseRef.orderByChild("destination")
                .startAt(searchBoxInput).endAt(searchBoxInput);//+ "\uf8ff");

        FirebaseRecyclerOptions<Post> findPlaceOptions = new FirebaseRecyclerOptions.Builder<Post>().setQuery(searchPlacesQuary, Post.class).build();

        firebaseRecycleAdapter = new FirebaseRecyclerAdapter<Post, FindPlacesViewHolder>(findPlaceOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FindPlacesViewHolder holder, int position, Post model) {
                final String PostKey = getRef(position).getKey();

                holder.username.setText(model.getFullname());
                holder.time.setText(String.format("%s", model.getTime()));
                holder.date.setText(String.format("%s", model.getDate()));
                holder.description.setText(model.getDescription());
                holder.destination.setText(model.getDestination());


                holder.setLikeButtonStatus(PostKey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent findPlaceIntent = new Intent(FindPlaceActivity.this, FindPlaceActivity.class);
                        findPlaceIntent.putExtra("PostKey", PostKey);
                        startActivity(findPlaceIntent);
                    }
                });

                // TODO: 09.04.2020  dodanie przejscia na postActivity  (visit_post_id)


                Picasso.get().load(model.getProfileimage()).into(holder.image);
                Picasso.get().load(model.getPostimage()).into(holder.postImage);


            }

            @NonNull
            @Override
            public FindPlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout, parent, false);
                return new FindPlacesViewHolder(view);
            }
        };
        SearchResultList.setAdapter(firebaseRecycleAdapter);
        firebaseRecycleAdapter.startListening();
    }


    private class FindPlacesViewHolder extends RecyclerView.ViewHolder {
        TextView username, date, time, description, destination;
        CircleImageView image;
        ImageView postImage;
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikesButton;
        int coutLikes;
        String currentUserId;
        DatabaseReference LikesRef;


        public FindPlacesViewHolder(View mView) {
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

