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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPlaceActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;
    private FirebaseRecyclerAdapter<FindPlace, FindPlacesViewHolder> firebaseRecycleAdapter;

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
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerOptions<FindPlace> findPlaceOptions = new FirebaseRecyclerOptions.Builder<FindPlace>().setQuery(searchPlacesQuary, FindPlace.class).build();

        firebaseRecycleAdapter = new FirebaseRecyclerAdapter<FindPlace, FindPlacesViewHolder>(findPlaceOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FindPlacesViewHolder holder, int position, FindPlace model) {
                final String PostKey = getRef(position).getKey();
                holder.myName.setText(model.getFullname());
                holder.postDestination.setText(model.getPlaceName());
                Picasso.get().load(model.getPostImage()).into(holder.postImage);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent findPlaceIntent = new Intent(FindPlaceActivity.this, FindPlaceActivity.class);
                        findPlaceIntent.putExtra("PostKey", PostKey);
                        startActivity(findPlaceIntent);
                    }
                });

                // TODO: 09.04.2020  dodanie przejscia na postActivity  (visit_post_id)


            }

            @NonNull
            @Override
            public FindPlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_places, parent, false);
                return new FindPlacesViewHolder(view);
            }
        };
        SearchResultList.setAdapter(firebaseRecycleAdapter);
        firebaseRecycleAdapter.startListening();
    }


    private class FindPlacesViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        TextView myName, postDestination;

        public FindPlacesViewHolder(View itemView) {
            super(itemView);

            postImage = itemView.findViewById(R.id.all_users_post_images);
            myName = itemView.findViewById(R.id.all_users_profile_name);
            postDestination = itemView.findViewById(R.id.all_users_post_places);

        }
    }
}
