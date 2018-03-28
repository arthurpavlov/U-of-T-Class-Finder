package tba.googleapitest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by siyuanzheng on 2018-03-07.
 */

public class CourseObject implements Parcelable{

    private String code;
    private String day;
    private String building;
    private String location;
    private String description;
    private String type; //TUT or LEC
    private String session;
    private String startTime;
    private String endTime;
    private String buildingCode;
    private String roomNum;

    public  CourseObject() {
        //default
    }

    public CourseObject(Parcel in) {
        code = in.readString();
        day = in.readString();
        building = in.readString();
        location = in.readString();
        description = in.readString();
        type = in.readString();
        session = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        buildingCode = in.readString();
        roomNum = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(day);
        dest.writeString(building);
        dest.writeString(location);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeString(session);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(buildingCode);
        dest.writeString(roomNum);
    }

    public static final Parcelable.Creator<CourseObject> CREATOR = new Parcelable.Creator<CourseObject>()
    {
        public CourseObject createFromParcel(Parcel in)
        {
            return new CourseObject(in);
        }
        public CourseObject[] newArray(int size)
        {
            return new CourseObject[size];
        }
    };

    public String getCode() {
        return code;
    }

    public String getDay() {
        return day;
    }
    public String getBuilding() {
        return building;
    }
    public String getLocation() {
        return location;
    }
    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getSession() {
        return session;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getbuildingCode() {
        return buildingCode;
    }

    public String getRoomNum() {
        return roomNum;
    }








    public void setBuildingCode(String a) {
        this.buildingCode = a ;
    }

    public void setRoomNum(String a) {
        this.roomNum = a;
    }

    public void setSession(String a) {
        this.session = a ;
    }

    public void setCode(String a) {
        this.code = a;
    }

    public void setDay(String a) {
        this.day = a;
    }

    public void setStartTime(String a) {
        this.startTime = a;
    }

    public void setEndTime(String a) {
        this.endTime = a;
    }

    public void setBuilding(String a) {
        this.building =  a;
    }

    public void setLocation(String a) {
        this.location = a;
    }

    public void setDescription(String a) {
        this.description = a;
    }

    public void setType(String a) {
        this.type = a;
    }



    public boolean equals(Object other) {
        return other instanceof CourseObject &&
                this.code == ((CourseObject) other).code &&
                this.day == ((CourseObject) other).day &&
                this.startTime == ((CourseObject) other).startTime &&
                this.endTime == ((CourseObject) other).endTime &&
                this.session == ((CourseObject) other).session &&
                //this.building == ((CourseObject) other).building &&
                //this.location == ((CourseObject) other).location &&
                //this.description == ((CourseObject) other).description &&
                this.type == ((CourseObject) other).type  &&
                this.buildingCode == ((CourseObject) other).buildingCode   &&
                this.roomNum == ((CourseObject) other).roomNum   ;
    }

    public String toString() {
        return "\nsession: " + this.session + "\ncode: " + this.code + "\nday: " + this.day + "\ntime: " + this.startTime + "-" + this.endTime +"\nbuilding: " + this.building
                +"\nlocation: "+ this.location +"\nbuilding code: " + this.buildingCode + "\nroom number: " + this.roomNum + "\ndescription: " + this.description + "\ntype: " + this.type + "\n";
    }


}
