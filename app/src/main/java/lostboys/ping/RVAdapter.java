package lostboys.ping;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import lostboys.ping.Models.EventEntry;

/**
 * Created by Syafiq on 23/7/2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{
    GoogleApiClient mGoogleApiClient;

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView day,month,time,event,par,des,host,place,loc;

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
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_card, viewGroup, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            final String[] name = new String[1];
            final String[] add = new String[1];
            personViewHolder.day.setText(String.valueOf(events.get(i).pickerDay));
            //day.setText(String.valueOf(events.get(i).pickerMonth));
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
            Places.GeoDataApi.getPlaceById( mGoogleApiClient, events.get(i).id ) .setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(PlaceBuffer places) {
                    if( places.getStatus().isSuccess() ) {
                        Place myPlace = places.get( 0 );
                        name[0] = (String) myPlace.getName();
                        add[0] = (String) myPlace.getAddress();

                    }

                    //Release the PlaceBuffer to prevent a memory leak
                    places.release();
                }
            } );
            personViewHolder.place.setText(name[0]);
            personViewHolder.loc.setText(add[0]);
            personViewHolder.des.setText(events.get(i).des);
            personViewHolder.par.setText(String.valueOf(events.get(i).members.size()));
            personViewHolder.host.setText(events.get(i).usr);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
}
