package lostboys.ping;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import lostboys.ping.Models.EventEntry;
import lostboys.ping.Pickers.Notification;

import static lostboys.ping.R.id.map;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener,
         OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "123";
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    private DatabaseReference mDatabase;
    private List<EventEntry> mEventEntries = new ArrayList<>();
    Button searchBtn;
    EditText addressET;
    PopupWindow changeSortPopUp;
    GoogleApiClient mGoogleApiClient;
    String text;    // spinner text
    private static final int GOOGLE_API_CLIENT_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().hide();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        populateMap();

        setupNavigationDrawer();

        // Spinner codes
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.event_spinner);
        String[] items = new String[] { "Event", "Place"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        dynamicSpinner.setAdapter(adapter);
        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                text = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        // address and search engine
        addressET = (EditText) findViewById(R.id.addressET);
        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBtn.getApplicationWindowToken(), 0);
                if (TextUtils.isEmpty(addressET.getText().toString())) {
                    Toast.makeText(MapsActivity.this, "Please enter.", Toast.LENGTH_LONG).show();
                } else {
                    String address = addressET.getText().toString();
                    updateMap(address);
                }
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    public void updateMap(String address) {          // search engine update
        mMap.clear();
        if (text.equals("Event")) {                  // search by event name
            for (EventEntry event : mEventEntries) {
                if (event.name.equals(address)) {
                    LatLng eventLoc = new LatLng(event.lat, event.lon);
                    Calendar cal = new GregorianCalendar();
                    cal.set(event.pickerYear, event.pickerMonth, event.pickerDay, event.pickerHour, event.pickerMin);
                    long time = cal.getTimeInMillis();
                    String formatted = (DateFormat.format("EEE, MMM d, 'at' HH:mm:ss", time))
                            .toString();
                    Marker mMarker = mMap.addMarker(new MarkerOptions().position(eventLoc).title(event.name).snippet(formatted + "/n" + String.valueOf(event.members.size()) + "joined."));
                    mMarker.setTag(event.key);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLoc));
                }
            }
        } else {                                      // search by place
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addressResult = geocoder.getFromLocationName(address, 1);
                if (!addressResult.isEmpty()) {
                    Address selectedResult = addressResult.get(0);
                    Double newLat = selectedResult.getLatitude();
                    Double newLong = selectedResult.getLongitude();
                    LatLng userLocation = new LatLng(newLat, newLong);
                    mMap.addMarker(new MarkerOptions().position(userLocation));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void populateMap(){
        mDatabase =  FirebaseDatabase.getInstance().getReference("events");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()){
                    EventEntry events = noteSnapshot.getValue(EventEntry.class);
                    mEventEntries.add(events);
                }
                for(EventEntry event : mEventEntries){
                    LatLng eventLoc = new LatLng(event.lat, event.lon);
                    Calendar cal = new GregorianCalendar();
                    cal.set(event.pickerYear,event.pickerMonth,event.pickerDay,event.pickerHour, event.pickerMin);
                    long time = cal.getTimeInMillis();
                    String formatted = (DateFormat.format("EEE, MMM d, 'at' HH:mm:ss", time))
                            .toString();
                    int size=event.members.size();
                    Marker mMarker = mMap.addMarker(new MarkerOptions().position(eventLoc)
                            .title(event.name)
                            .snippet(formatted+",with "+String.valueOf(size)+" joining.")
                            .icon(BitmapDescriptorFactory.defaultMarker(20))
                            .alpha(0.6f));
                    mMarker.setTag(event.key);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, databaseError.getMessage());
            }
        });
    }

    // Navigation Drawer
    private void setupNavigationDrawer() {
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(0).withName(R.string.drawer_item_home);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_notification);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_past_events);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_item_rewards);
        SecondaryDrawerItem item7 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(6).withName(R.string.drawer_item_about_us).withIcon(FontAwesome.Icon.faw_info_circle);
        SecondaryDrawerItem item8 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(7).withName(R.string.drawer_item_contact_us).withIcon(FontAwesome.Icon.faw_whatsapp);
        SecondaryDrawerItem item9 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(8).withName(R.string.drawer_item_feedback).withIcon(FontAwesome.Icon.faw_commenting);
        SecondaryDrawerItem item10 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(9).withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_wrench);

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.md_black_1000)
                .addProfiles(
                        new ProfileDrawerItem().withName(getResources().getString(R.string.app_name))
                )
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        //Any activity or Intent
                        return true;
                    }
                })
                .build();
        //Create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1, item2, item3, item4,
                        new SectionDrawerItem().withName("Extras"),
                        item7, item8, item9, item10
                )

                //Set onClick options for drawer item click
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch (position) {
                            //Home
                            case 1: {
                                break;
                            }
                            // View Notifications
                            case 2: {
                                Intent myIntent = new Intent(MapsActivity.this, Notification.class);
                                startActivity(myIntent);
                                break;
                            }

                            // View Past Events joined
                            case 3: {
                                break;
                            }

                            // View Rewards
                            case 4: {
                                break;
                            }

                            //About us
                            case 5: {
                                break;
                            }

                            //Contact Us
                            case 6: {
                                break;
                            }

                            //Feedback
                            case 7: {
                                break;
                            }

                            //Settings
                            case 8:{
                                break;
                            }

                        }
                        return true;
                    }
                })
                .build();

//        result.addStickyFooterItem(new PrimaryDrawerItem().withName("Visit us again")); //Adding footer to nav drawer
    }

    // Event create button
    public void onButtonClick(View view){
        Intent intent = new Intent(MapsActivity.this, EventCreate.class);
        startActivity(intent);
    }

    // Drop down menu
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    // menu options including logout
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.logout: {
//                FirebaseAuth.getInstance().signOut();
//                LoginManager.getInstance().logOut();
//                Intent myIntent = new Intent(MapsActivity.this, FacebookLoginActivity.class);
//                startActivity(myIntent);
//            }
//        }
//        return true;
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check gps location granted
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setPadding(0,0,0,0);
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // ask for gps permission
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                // soc latlng = 1.2950362, 103.7717432
                LatLng userLocation = new LatLng(1.2950362, 103.7717432);
                mMap.clear();
               // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
        mMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.popout, viewGroup);
        final TextView day,month,time,event,par,des,host;

        changeSortPopUp = new PopupWindow(this);
        changeSortPopUp.setContentView(layout);
        changeSortPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeSortPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeSortPopUp.setFocusable(true);

        // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
        int OFFSET_X = -20;
        int OFFSET_Y = 95;

        // Clear the default translucent background
        //changeSortPopUp.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        changeSortPopUp.showAtLocation(layout, Gravity.CENTER,0,0);

        // Getting a reference to Close button, and close the popup when clicked.
        Button close = (Button) layout.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changeSortPopUp.dismiss();
            }
        });

        day=(TextView) layout.findViewById(R.id.text_view_day);
        month=(TextView) layout.findViewById(R.id.text_view_month);
        time=(TextView) layout.findViewById(R.id.text_view_time);
        event=(TextView) layout.findViewById(R.id.text_view_event);
        des=(TextView) layout.findViewById(R.id.text_view_des);
        host=(TextView) layout.findViewById(R.id.text_view_host);
        par=(TextView) layout.findViewById(R.id.text_view_no_par);
        for(EventEntry temp : mEventEntries) {
            if (temp.key.equals(marker.getTag())) {
                day.setText(String.valueOf(temp.pickerDay));
                //day.setText(String.valueOf(temp.pickerMonth));
                Calendar cal = new GregorianCalendar();
                cal.set(temp.pickerYear,temp.pickerMonth,temp.pickerDay,temp.pickerHour, temp.pickerMin);
                long time_num = cal.getTimeInMillis();
                String formatted = (DateFormat.format("MMM", time_num))
                        .toString();
                month.setText(formatted);
                formatted = (DateFormat.format("hh:mm a", time_num))
                        .toString();
                time.setText(formatted);
                event.setText(temp.name);
                Places.GeoDataApi.getPlaceById( mGoogleApiClient, temp.id ) .setResultCallback( new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if( places.getStatus().isSuccess() ) {
                            Place myPlace = places.get( 0 );
                            TextView place=(TextView) layout.findViewById(R.id.text_view_location_place);
                            TextView loc=(TextView) layout.findViewById(R.id.text_view_location);
                            place.setText(myPlace.getName());
                            loc.setText(myPlace.getAddress());
                        }

                        //Release the PlaceBuffer to prevent a memory leak
                        places.release();
                    }
                } );

                des.setText(temp.des);
                par.setText(String.valueOf(temp.members.size()));
                host.setText(temp.usr);
            }
        }
    }
}
