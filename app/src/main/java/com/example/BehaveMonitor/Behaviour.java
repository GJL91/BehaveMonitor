package com.example.BehaveMonitor;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by BJD on 06/12/2014.
 */
public class Behaviour implements Parcelable {
	//Behaviour Name
	String bName;
	
	//0 = Event
	//1 = State
	int type;
	
	//Holds the events for this behaviour.
	ArrayList<Event> eventHistory = new ArrayList<Event>();
	
	//Holds the current state event before its ended and added to the history.
	Event currentEvent;

    Behaviour(){};

    Behaviour(Parcel in) {
        bName = in.readString();
        type = in.readInt();
        eventHistory = new ArrayList<Event>();
        in.readTypedList(eventHistory,Event.CREATOR);
        currentEvent = in.readParcelable(Event.class.getClassLoader());
    }

    public static final Parcelable.Creator<Behaviour> CREATOR = new Parcelable.Creator<Behaviour>() {

        public Behaviour createFromParcel(Parcel in) {
            return new Behaviour(in);
        }

        public Behaviour[] newArray(int size) {
            return new Behaviour[size];
        }
    };


	
	public void newEvent(int eType) {
		this.type = eType;
		if(eType==0) {
			Event event = new Event();
			event.duration = "";
			eventHistory.add(event);
		} else if(eType == 1) {
			Event event = new Event();
			currentEvent = event;
		} else {
			Log.e("Event type error", "the eType was not a 1 or a 0");
			//ERROR
		}
	}
	
	//Ends the current event of this behaviour type, and adds it to the history reseting the currentEvent variable.
	public void endCurrentEvent() {
		if (currentEvent != null) {
			currentEvent.end();
			eventHistory.add(currentEvent);
			currentEvent = null;
		}
	}
	
	//Returns the behaviour and all its events for session Saving
	public String toString() {
		//Get name and type of behaviour
		String out = this.bName + "/nType = " + this.type + ":\n\n";
		SimpleDateFormat sdf = new SimpleDateFormat("H.m.s.S");
		
		//If type0 Event just the start time
		String startTimes = "Start Times:";
		if(this.type==0) {
			for (Event e: this.eventHistory) {
				startTimes += "," + sdf.format(e.startTime);
			}
		
		//else type1 state durations and start times.
		} else if(this.type == 1) {
			String durations = "Durations:";
			for (Event e: this.eventHistory) {
				startTimes += "," + sdf.format(e.startTime);
				durations += "," + e.duration;
			}
			durations += "/n";
			out += durations;
		} else {
			Log.e("Event type error", "the eType was not a 1 or a 0");
			//ERROR
		}
		startTimes += "/n";
		out += startTimes;
		return out;
	}



    //Stuff to make it parcelable.


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bName);
        dest.writeInt(type);
        dest.writeTypedList(eventHistory);
        dest.writeParcelable(currentEvent, flags);

    }
}
