/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

/**
 * The main activity of the API library demo gallery.
 * <p>
 * The main layout lists the demonstrated features, with buttons to launch them.
 */
public final class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private Firebase firebase;

    /**
     * A custom array adapter that shows a {@link FeatureView} containing details about the demo.
     */
    private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {

        /**
         * @param demos An array containing the details of the demos to be displayed.
         */
        public CustomArrayAdapter(Context context, DemoDetails[] demos) {
            super(context, R.layout.feature, R.id.title, demos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {
                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }

            DemoDetails demo = getItem(position);

            featureView.setTitleId(demo.titleId);
            featureView.setDescriptionId(demo.descriptionId);

            Resources resources = getContext().getResources();
            String title = resources.getString(demo.titleId);
            String description = resources.getString(demo.descriptionId);
            featureView.setContentDescription(title + ". " + description);

            return featureView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Firebase.setAndroidContext(this);
        ListView list = (ListView) findViewById(R.id.list);

        ListAdapter adapter = new CustomArrayAdapter(this, DemoDetailsList.DEMOS);

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setEmptyView(findViewById(R.id.empty));
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebase = new Firebase("https://poopoopoint.firebaseio.com");

        firebase.child("codyTest0").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.println(Log.INFO, "MainActivity", snapshot.getValue().toString());
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.println(Log.ERROR, "MainActivity", "The read failed: " + firebaseError.getMessage());
            }
        });

        Firebase washroomsRef = firebase.child("Washrooms");
        washroomsRef.push().setValue(new Washroom(new LatLng(0, 0), "zero-zero"));
        washroomsRef.push().setValue(new Washroom(new LatLng(4234.234, -203.23), "somewhere").thumbsUp().thumbsUp().thumbsDown());
        washroomsRef.push().setValue(new Washroom(new LatLng(5, -5), "jaja").thumbsDown());

        Firebase fountainsRef = firebase.child("Fountains");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.menu_legal) {
            startActivity(new Intent(this, LegalInfoActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DemoDetails demo = (DemoDetails) parent.getAdapter().getItem(position);
        startActivity(new Intent(this, demo.activityClass));
    }

    public class Washroom {
        private LatLng latLng;
        private String name;
        private int thumbsUp;
        private int thumbsDown;

        public Washroom(LatLng latLng, String name) {
            this.latLng = latLng;
            this.name = name;
            this.thumbsUp = 0;
            this.thumbsDown = 0;
        }

        public Washroom thumbsUp() {
            this.thumbsUp++;
            return this;
        }

        public Washroom thumbsDown() {
            this.thumbsDown++;
            return this;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latLng.latitude;
        }

        public double getLongitude() {
            return latLng.longitude;
        }

        public double getRating() {
            int total = thumbsUp + thumbsDown;
            if (total != 0) {
                return (double)thumbsUp/total;
            } else {
                return -1;
            }
        }

        public String toString() {
            return name + " " + latLng.toString();
        }
    }
}
