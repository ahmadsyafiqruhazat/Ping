package lostboys.ping.Models;

import java.util.ArrayList;

/**
 * Created by Syafiq on 28/6/2017.
 */

public class EventEntry {

    public String name,des,key;
    public int pickerHour,pickerMin,pickerYear,pickerMonth,pickerDay;
    public double lat, lon;
    public ArrayList<String> members;

    public EventEntry(){
    }

    public EventEntry(String name, int pickerHour,int pickerMin,int pickerYear,int pickerMonth,int pickerDay, String des, double lat, double lon, String key, ArrayList<String> members){
        this.name=name;
        this.pickerHour= pickerHour;
        this.pickerMin = pickerMin;
        this.pickerYear = pickerYear;
        this.pickerMonth = pickerMonth;
        this.pickerDay = pickerDay;
        this.lat = lat;
        this.lon = lon;
        this.des=des;
        this.key=key;
        this.members=members;
    }

}
