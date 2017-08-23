package lostboys.ping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
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
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import lostboys.ping.Models.EventEntry;
import lostboys.ping.Models.Profile;
import lostboys.ping.Pickers.AboutUs;
import lostboys.ping.Pickers.Feedback;

import static lostboys.ping.R.id.map;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener,
         OnMapReadyCallback {

    private static final String LOG_TAG = "123";
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    private DatabaseReference mDatabase;
    private List<EventEntry> mEventEntries = new ArrayList<>();
    Button searchBtn;
    EditText addressET;
    PopupWindow changeSortPopUp;
    View mapView;

    String text;    // spinner text

    Profile obj;
    Bitmap pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        getSupportActionBar().hide();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        populateMap();
        SharedPreferences mPrefs = getSharedPreferences("myPrefs",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("User", "");

        obj = gson.fromJson(json, Profile.class);
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }
                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()
                return super.placeholder(ctx, tag);
            }
        });
        setupNavigationDrawer();

        // Spinner codes
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.event_spinner);
        String[] items = new String[] { "Event", "Place", "Category"};
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
                    int size = event.members.size();
                    Marker mMarker = mMap.addMarker(new MarkerOptions().position(eventLoc)
                            .title(event.name)
                            .snippet(formatted+",with "+String.valueOf(size)+" joining.")
                            .icon(BitmapDescriptorFactory.defaultMarker(20))
                            .alpha(1f));
                    mMarker.setTag(event.key);
                    switch (event.category){
                        case "Food":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food));
                            break;
                        case "Party":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.party));
                            break;
                        case "Sports":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sports));
                            break;
                        case "Music":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.music));
                            break;
                        case "Shopping":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.shopping));
                            break;
                        default:
                            break;
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLoc));
                }
            }
        }
        if (text.equals("Category")) {  // search by event category
            for (EventEntry event: mEventEntries){
                if (event.category.equals(address)){
                    LatLng eventLoc = new LatLng(event.lat, event.lon);
                    Calendar cal = new GregorianCalendar();
                    cal.set(event.pickerYear, event.pickerMonth, event.pickerDay, event.pickerHour, event.pickerMin);
                    long time = cal.getTimeInMillis();
                    String formatted = (DateFormat.format("EEE, MMM d, 'at' HH:mm:ss", time))
                            .toString();
                    int size = event.members.size();
                    Marker mMarker = mMap.addMarker(new MarkerOptions().position(eventLoc)
                            .title(event.name)
                            .snippet(formatted+",with "+String.valueOf(size)+" joining.")
                            .icon(BitmapDescriptorFactory.defaultMarker(20))
                            .alpha(1f));
                    mMarker.setTag(event.key);
                    switch (event.category){
                        case "Food":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food));
                            break;
                        case "Party":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.party));
                            break;
                        case "Sports":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sports));
                            break;
                        case "Music":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.music));
                            break;
                        case "Shopping":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.shopping));
                            break;
                        default:
                            break;
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLoc));
                }
            }
        } else{                                      // search by place
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
                        for(EventEntry event : mEventEntries) {
                            LatLng eventLoc = new LatLng(event.lat, event.lon);
                            Calendar cal = new GregorianCalendar();
                            cal.set(event.pickerYear, event.pickerMonth, event.pickerDay, event.pickerHour, event.pickerMin);
                            long time = cal.getTimeInMillis();
                            String formatted = (DateFormat.format("EEE, MMM d, 'at' HH:mm:ss", time))
                                    .toString();
                            int size = event.members.size();
                            Marker mMarker = mMap.addMarker(new MarkerOptions().position(eventLoc)
                                    .title(event.name)
                                    .snippet(formatted + ",with " + String.valueOf(size) + " joining.")
                                    .icon(BitmapDescriptorFactory.defaultMarker(20))
                                    .alpha(1f));
                            mMarker.setTag(event.key);
                            switch (event.category) {
                                case "Food":
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food));
                                    break;
                                case "Party":
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.party));
                                    break;
                                case "Sports":
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sports));
                                    break;
                                case "Music":
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.music));
                                    break;
                                case "Shopping":
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.shopping));
                                    break;
                                default:
                                    break;
                            }

                        }
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
                            .alpha(1f));
                    mMarker.setTag(event.key);
                    switch (event.category){
                        case "Food":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food));
                            break;
                        case "Party":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.party));
                            break;
                        case "Sports":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sports));
                            break;
                        case "Music":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.music));
                            break;
                        case "Shopping":
                            mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.shopping));
                            break;
                        default:
                            break;
                    }

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
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Rewards");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Created Events");
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Joined Events");
        SecondaryDrawerItem item4 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(4).withName(R.string.drawer_item_about_us).withIcon(FontAwesome.Icon.faw_info_circle);
        SecondaryDrawerItem item5 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(5).withName(R.string.drawer_item_feedback).withIcon(FontAwesome.Icon.faw_commenting);
        SecondaryDrawerItem item6 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(6).withName("Logout").withIcon(FontAwesome.Icon.faw_sign_out);

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.yellow)
                .addProfiles(
                        new ProfileDrawerItem().withName(obj.userName).withIcon("https://graph.facebook.com/"+obj.picID+"/picture?type=normal")
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
                        item1, item2, item3,
                        new SectionDrawerItem().withName("Extras"),
                        item4, item5, item6
                )

                //Set onClick options for drawer item click
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch (position) {

                            // View Notifications
                            case 1: {
                                Intent myIntent = new Intent(MapsActivity.this, Rewards.class);
                                startActivity(myIntent);
                                break;
                            }
                            // View Past Events joined
                            case 2: {
                                Intent myIntent = new Intent(MapsActivity.this, Created.class);
                               startActivity(myIntent);
                                break;
                            }
                            // View Rewards
                            case 3: {
                                Intent myIntent = new Intent(MapsActivity.this, Joined.class);
                                startActivity(myIntent);
                                break;
                            }
                            //About us
                            case 5: {
                                Intent myIntent = new Intent(MapsActivity.this, AboutUs.class);
                                startActivity(myIntent);
                                break;
                            }
                            //Feedback
                            case 6: {
                                Intent myIntent = new Intent(MapsActivity.this, Feedback.class);
                                startActivity(myIntent);
                                break;
                            }
                            // Logout
                            case 7:{
                                FirebaseAuth.getInstance().signOut();
                                LoginManager.getInstance().logOut();
                                Intent myIntent = new Intent(MapsActivity.this, FacebookLoginActivity.class);
                                startActivity(myIntent);
                                break;
                            }
                        }
                        return true;
                    }
                })
                .build();
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

//            @Override
//            public Drawable placeholder(Context ctx, String tag) {
//                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
//                    return DrawerUIUtils.getPlaceHolder(ctx);
//                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
//                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
//                } else if ("customUrlItem".equals(tag)) {
//                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
//                }
//
//                //we use the default one for
//                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()
//
//                return super.placeholder(ctx, tag);
//            }

    });


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
        mMap.setMyLocationEnabled(true);
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 250);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
            }}

        mMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.popout, viewGroup);
        final TextView day,month,time,eventName,par,des,host;
        EventEntry tempEvent= new EventEntry();
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
        Button join = (Button) layout.findViewById(R.id.join);
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        day=(TextView) layout.findViewById(R.id.text_view_day);
        month=(TextView) layout.findViewById(R.id.text_view_month);
        time=(TextView) layout.findViewById(R.id.text_view_time);
        eventName=(TextView) layout.findViewById(R.id.text_view_event);
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
                eventName.setText(temp.name);

                TextView place=(TextView) layout.findViewById(R.id.text_view_location_place);
                TextView loc=(TextView) layout.findViewById(R.id.text_view_location);
                place.setText(temp.loc);
                loc.setText(temp.add);
                des.setText(temp.des);
                par.setText(String.valueOf(temp.members.size()));
                host.setText(temp.usr);
                temp.members.add(mFirebaseUser.getUid());
                tempEvent=temp;
            }
        }
        final EventEntry finalTempEvent = tempEvent;
        join.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference("users").child(mFirebaseUser.getUid()).child("profile").child("eventsJoined").child(finalTempEvent.key);
                DatabaseReference mDatabase2 =  FirebaseDatabase.getInstance().getReference("events").child(finalTempEvent.key).child("members");
                mDatabase.setValue(finalTempEvent);
                mDatabase2.setValue(finalTempEvent.members);
                Toast.makeText(getApplicationContext(),"Event Joined",Toast.LENGTH_SHORT).show();
                changeSortPopUp.dismiss();

            }
        });
    }



}
