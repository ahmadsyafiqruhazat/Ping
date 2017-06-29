package lostboys.ping.Models;

/**
 * Created by Syafiq on 28/6/2017.
 */

public class EventEntry {

    public String name,des;
    public int pickerHour,pickerMin,pickerYear,pickerMonth,pickerDay;


    public EventEntry(){
    }

    public EventEntry(String name, int pickerHour,int pickerMin,int pickerYear,int pickerMonth,int pickerDay, String des){
        this.name=name;
        this.pickerHour= pickerHour;
        this.pickerMin = pickerMin;
        this.pickerYear = pickerYear;
        this.pickerMonth = pickerMonth;
        this.pickerDay = pickerDay;
        this.des=des;
    }

}
