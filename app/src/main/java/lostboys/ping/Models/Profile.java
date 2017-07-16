package lostboys.ping.Models;

import java.util.ArrayList;

/**
 * Created by Syafiq on 12/7/2017.
 */

public class Profile {
    public String userName, picID;
    public ArrayList<EventEntry> eventsCreated, eventsJoined;

    public Profile(){}

    public Profile(String userName, String picId){
        this.userName = userName;
        this.picID = picId;
        eventsCreated = new ArrayList<>();
        eventsJoined = new ArrayList<>();
    }

    public Profile(String userName, String picId, ArrayList<EventEntry> eventsCreated, ArrayList<EventEntry> eventsJoined){
        this.userName = userName;
        this.picID = picId;
        this.eventsCreated = eventsCreated;
        this.eventsJoined = eventsJoined;
    }

}
