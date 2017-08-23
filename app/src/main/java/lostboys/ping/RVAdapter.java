package lostboys.ping;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import lostboys.ping.Models.EventEntry;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Syafiq on 23/7/2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{
    int j,y,x;
    String profileID;
    PersonViewHolder hold;

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView day,month,time,event,par,des,host,place,loc;
        Button delete;
        ProfilePictureView member1,member2,member3;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            day=(TextView) itemView.findViewById(R.id.text_view_day);
            month=(TextView) itemView.findViewById(R.id.text_view_month);
            time=(TextView) itemView.findViewById(R.id.text_view_time);
            event=(TextView) itemView.findViewById(R.id.text_view_event);
            des=(TextView) itemView.findViewById(R.id.text_view_des);
            host=(TextView) itemView.findViewById(R.id.text_view_host);
            par=(TextView) itemView.findViewById(R.id.text_view_no_par);
            place=(TextView) itemView.findViewById(R.id.text_view_location_place);
            loc=(TextView) itemView.findViewById(R.id.text_view_location);
            delete=(Button) itemView.findViewById(R.id.delete_button);
            member1=(ProfilePictureView) itemView.findViewById(R.id.imageView1);
            member2=(ProfilePictureView) itemView.findViewById(R.id.imageView2);
            member3=(ProfilePictureView) itemView.findViewById(R.id.imageView3);
        }
    }
        ArrayList<EventEntry> events;

        RVAdapter(ArrayList<EventEntry> events){
            this.events = events;
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_created_card, viewGroup, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            j=i;
            hold = personViewHolder;
            personViewHolder.day.setText(String.valueOf(events.get(i).pickerDay));
            Calendar cal = new GregorianCalendar();
            cal.set(events.get(i).pickerYear,events.get(i).pickerMonth,events.get(i).pickerDay,events.get(i).pickerHour, events.get(i).pickerMin);
            long time_num = cal.getTimeInMillis();
            String formatted = (DateFormat.format("MMM", time_num))
                    .toString();
            personViewHolder.month.setText(formatted);
            formatted = (DateFormat.format("hh:mm a", time_num))
                    .toString();
            personViewHolder.time.setText(formatted);
            personViewHolder.event.setText(events.get(i).name);

            personViewHolder.des.setText(events.get(i).des);
            personViewHolder.par.setText(String.valueOf(events.get(i).members.size()));
            personViewHolder.host.setText(events.get(i).usr);
            //Toast.makeText(getApplicationContext(),"events loaded",Toast.LENGTH_SHORT).show();
            personViewHolder.loc.setText(events.get(i).add);
            DatabaseReference mDatabase1 =  FirebaseDatabase.getInstance().getReference("users");


                mDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        x=events.get(j).members.size();
                        y=0;
                        while(x>y) {
                            profileID = (String) dataSnapshot.child(events.get(j).members.get(y)).child("profile").child("picID").getValue();
                            if (y==0) {
                                hold.member1.setProfileId(profileID);
                            } else if (y==1) {
                                hold.member2.setProfileId(profileID);
                            } else if (y==2) {
                                hold.member3.setProfileId(profileID);
                            }
                            y++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    }
                });

            personViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
                    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                    DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference();
                    for(String name: events.get(j).members){
                        mDatabase.child("users").child(name).child("profile").child("eventsJoined").child(events.get(j).key).child("members").child(mFirebaseUser.getUid()).removeValue();
                    }
                    mDatabase.child("users").child(mFirebaseUser.getUid()).child("profile").child("eventsJoined").child(events.get(j).key).removeValue();
                    Toast.makeText(getApplicationContext(),"Event Removed",Toast.LENGTH_SHORT).show();

                    notifyDataSetChanged();
                }
            });
            FirebaseMessaging.getInstance().subscribeToTopic("news");
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
}
