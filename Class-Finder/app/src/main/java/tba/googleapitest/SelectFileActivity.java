package tba.googleapitest;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by peilin on 2016-11-29. For switch user's purpose Admin can select user to edit Client
 * can only select itself, Use spinner to select in layout.
 */

public class SelectFileActivity extends AppCompatActivity {
  private static final String EXTRA_FLIGHT = "Flight";
  private Spinner userSpinner;
  private Spinner flightSpinner;
  private int userPos;
  private String author;
  public SharedPreferences files;
  private String resultPath;
  private Button btChoose;
  private String filePath;
  //NOTE: not sure whether should code_course be initialized here or under onCreate/Constructor of SelectFileActivity
  private HashMap<String, ArrayList<CourseObject>> code_course = new HashMap<String, ArrayList<CourseObject>>();
  private ArrayList<CourseObject> courses;
  private String userEmail;
  private static String serverAddr = "http://165.227.34.218";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_selector);
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(SelectFileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }
    btChoose = (Button) findViewById(R.id.bt_choose);
    courses = null;


    btChoose.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "choose file"), 0);
      }
    });
    Bundle b = getIntent().getExtras();
    userEmail = b.getString("email");
    fetchCalendarFromDB(userEmail);
  }
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    try {
        Uri uri = data.getData();
        parse(uri);
    } catch (IOException e) {
        Log.e("IOException", "parser threw IOException, "+e.toString());
    } catch (NullPointerException ne) {
        Log.e("NullException", "parser threw NullException, "+ne.toString());
    }
    courses = getCalendar();
    Log.i("Course data", courses.toString());
    if (courses != null || !courses.isEmpty()) {
        Toast.makeText(this, "Calendar file has been selected!", Toast.LENGTH_LONG).show();
    }
    //Serialize courses and upload to server
    if(courses != null){
      uploadCalendarToServer(courses);
    }
     super.onActivityResult(requestCode, resultCode, data);
  }




  /**
   * @param view The instance of the widget that was clicked.
   */
  public void switchToMap(View view) {
    // Transition between activities
    if (courses != null) {
      Intent moveIntent = new Intent(this, MapsActivity.class);
      Bundle b = new Bundle();
      Log.i("tomap", courses.toString());
      b.putParcelableArrayList("courses", courses);
      moveIntent.putExtras(b);
      startActivity(moveIntent);
    }
    else{
      Toast.makeText(this, "Calendar File Not Selected!", Toast.LENGTH_LONG).show();
    }
  }

  public void uploadCalendarToServer(ArrayList<CourseObject> courses){
    String serializedCal = serializeCalendar(courses);
    String reqUrl = serverAddr + "/usercalendars/"+userEmail;
    JSONObject reqObj = null;
    try {
      reqObj = new JSONObject();
      reqObj.put("calendar", serializedCal);
    } catch (JSONException e) {
      Log.e("JSONException", "Failed to build json for addresses="+reqUrl);
    }
    RequestQueue queue = Volley.newRequestQueue(this);
    JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.PUT, reqUrl, reqObj, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {
        Log.i("res", response.toString());
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.i("res", "response="+error);
      }
    });
    queue.add(jsonReq);
  }

  public void fetchCalendarFromDB(String email){
    String reqUrl = serverAddr + "/users/"+email;
    RequestQueue queue = Volley.newRequestQueue(this);
    StringRequest strReq = new StringRequest(Request.Method.GET, reqUrl, new Response.Listener<String>() {
      public void onResponse(String response) {
        deserializeCalendar(response);
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e("buildings error", error.toString());
      }
    });
    queue.add(strReq);
  }

  public String serializeCalendar(ArrayList<CourseObject> courses){
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i< courses.size(); i++){
      sb.append("{\"session\":\"" + courses.get(i).getSession() + "\",");
      sb.append("\"code\":\"" + courses.get(i).getCode() + "\",");
      sb.append("\"day\":\"" + courses.get(i).getDay() + "\",");
      sb.append("\"time\":\"" + courses.get(i).getStartTime() + "-" + courses.get(i).getEndTime() + "\",");
      sb.append("\"building\":\"" + courses.get(i).getBuilding() + "\",");
      sb.append("\"location\":\"" + courses.get(i).getLocation() + "\",");
      sb.append("\"building code\":\"" + courses.get(i).getbuildingCode() + "\",");
      sb.append("\"room number\":\"" + courses.get(i).getRoomNum() + "\",");
      sb.append("\"description\":\"" + courses.get(i).getDescription() + "\",");
      sb.append("\"type\":\"" + courses.get(i).getType() + "\"}|");
    }
    sb.deleteCharAt(sb.toString().length()-1);
    Log.i("stringbuilder", sb.toString());
    return sb.toString();
  }

  public void deserializeCalendar(String response){
    ArrayList<CourseObject> dbCourses = new ArrayList<CourseObject>();
    try{
      JSONObject obj = new JSONObject(response);
      String calendar = obj.getString("success");
      if(!calendar.isEmpty()){
        //create courses object and add to courses
        Log.i("calendar", calendar);
        String[] dbCoursesSplit = calendar.split("\\|");
        for(int i = 0; i< dbCoursesSplit.length; i++){
          CourseObject dbCourse = new CourseObject();
          JSONObject jsonObj = new JSONObject(dbCoursesSplit[i]);
          String[] timeSplit = jsonObj.getString("time").split("\\-");
          dbCourse.setSession(jsonObj.getString("session"));
          dbCourse.setCode(jsonObj.getString("code"));
          dbCourse.setDay(jsonObj.getString("day"));
          dbCourse.setStartTime(timeSplit[0]);
          dbCourse.setEndTime(timeSplit[1]);
          dbCourse.setBuilding(jsonObj.getString("building"));
          dbCourse.setLocation(jsonObj.getString("location"));
          dbCourse.setBuildingCode(jsonObj.getString("building code"));
          dbCourse.setRoomNum(jsonObj.getString("room number"));
          dbCourse.setDescription(jsonObj.getString("description"));
          dbCourse.setType(jsonObj.getString("type"));
          dbCourses.add(dbCourse);
        }
        Log.i("final", dbCourses.toString());
        courses = dbCourses;
      }
      else{
        Log.i("calendar", "calendar file not uploaded");
      }
    }
    catch (JSONException e) {
      Log.i("calendar", e.toString());
    }
  }





  //file parser, it used to be a standalone class , and right now a method




  //NOTE: right now the implementation only contains schedule of one user
  public void parse(Uri uri) throws IOException {
    //the key of the hashMap is a  course code while the value of the key is the corresponding course object
      InputStream is = getContentResolver().openInputStream(uri);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
    StringBuffer stringBuffer = new StringBuffer();
    String line;


    while ((line = bufferedReader.readLine()) != null) {
      stringBuffer.append(line);
      stringBuffer.append("\n");
    }
    bufferedReader.close();

    String new_s = stringBuffer.toString();
    String[] new_s_split = new_s.split("SUMMARY:");
    //get rid of the first element, which is not a actual course information
    new_s_split = Arrays.copyOfRange(new_s_split, 1, new_s_split.length);

    String courseCode;
    String type;
    String description;
    String buildingName;
    String location;
    String dtstart;
    String dtend;
    String session; //denote the course whether is winter/fall/summer first/summer second
    String day;
    String startTime;
    String endTime;
    String startDate;
    Date localDate = null;
    String until;
    String buildingCode;
    String roomNum;

    int temp;

    for (String s : new_s_split) {
      //append course information here
      courseCode = s.substring(0,s.indexOf(" ")).trim();
      //System.out.println( courseCode );

      type = s.substring(s.indexOf(" "),s.indexOf("\n")).trim();
      //System.out.println(type);

      temp = s.indexOf("DESCRIPTION");
      description = s.substring(temp + "DESCRIPTION".length() + 1 ,s.indexOf("\\n", temp)).trim();
      //System.out.println(description );


      buildingName = s.substring(s.indexOf("\\n", temp) + 2 ,s.indexOf("\n", s.indexOf("\\n", temp))).trim();
      //System.out.println( buildingName);

      temp = s.indexOf("LOCATION");
      location = s.substring(temp + "location".length() + 1 , s.indexOf("\n", temp)).trim();
      //System.out.println( location);

      buildingCode = location.substring(0,location.indexOf(" "));
      roomNum = location.substring(location.indexOf(" ") + 1 );


      temp = s.indexOf("dtstart");
      dtstart = s.substring(s.indexOf(":",temp)+ 1, s.indexOf("\n",s.indexOf(":",temp)));

      startDate = dtstart.substring(0,8);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      try {
        localDate = sdf.parse(startDate);
      } catch (ParseException e) {
        Log.e("ParseException", "error parsing date="+startDate);
      }
      //localDate = LocalDate.of(Integer.parseInt(startDate.substring(0,4)), Integer.parseInt(startDate.substring(4,6)), Integer.parseInt(startDate.substring(6,8)));
      //System.out.println(localDate);
      //System.out.println(localDate.getDayOfWeek());
      //System.out.println(startDate.substring(0,4) +  startDate.substring(4,6) + startDate.substring(6,8));


      until = s.substring(s.toLowerCase().indexOf("until") + 6 , s.indexOf("\n", s.toLowerCase().indexOf("until"))).substring(4,6);
      //System.out.println(until);

      if(dtstart.substring(4,6).equals("09")) {
        session = "Fall";
      }else if(dtstart.substring(4, 6).equals("01")) {
        session = "Winter";
      }else if(dtstart.substring(4,6).equals("05")) {
        if(until.equals("06")) {
          session = "Summer-First";
        }else if(until.equals("08")) {
          session = "Summer-Y";
        }else {
          session = "Summer-Undefined";
        }
      }else if(dtstart.substring(4,6).equals("07")) {
        session = "Summer-Second";
      }else {
        session = "Undefined";
      }
      //System.out.println(session);

//		    if(dtstart.substring(8,9).equals("M")) {
//	    			day = "Monday";
//		    }else if(dtstart.substring(8,9).equals("T")) {
//		    		day = "Tuesday";
//		    }else if(dtstart.substring(8,9).equals("W")) {
//		    		day = "Wednesday";
//		    }else if(dtstart.substring(8,9).equals("R")) {
//		    		day = "Thursday";
//		    }else if(dtstart.substring(8,9).equals("R")) {
//	    			day = "Friday";
//		    }else {
//		    		day = "Undefined";
//		    }

      Calendar cal = Calendar.getInstance();
      cal.setTime(localDate);
      //day = localDate.getDayOfWeek().toString();
      day = Integer.toString(cal.get(Calendar.DAY_OF_WEEK));



      //System.out.println(day);

      startTime = dtstart.substring(9,13).trim();
      //System.out.println(startTime);

      temp = s.toLowerCase().indexOf("dtend");
      dtend = s.substring(temp,s.indexOf("\n",temp));

      temp = dtend.indexOf(":");

      endTime = dtend.substring(temp + 1).substring(9,13).trim();
      //System.out.println(endTime);



      CourseObject co = new CourseObject();

      co.setCode(courseCode);
      co.setType(type);
      co.setDescription(description);
      co.setBuilding(buildingName);
      co.setLocation(location);
      co.setSession(session);
      co.setDay(day);
      co.setStartTime(startTime);
      co.setEndTime(endTime);
      co.setBuildingCode(buildingCode);
      co.setRoomNum(roomNum);


      if(!code_course.containsKey(courseCode)) {
        ArrayList<CourseObject> list_temp = new ArrayList<CourseObject>();
        list_temp.add(co);
        code_course.put(courseCode, list_temp);
      }else {
        code_course.get(courseCode).add(co);
      }




      //System.out.println("--------------------\n");
    }


  }


  public HashMap<String, ArrayList<CourseObject>> getParsedResult() {
    return code_course;
  }

  //This clears everything in the parser
  public void restoreParser() {
    code_course.clear();
  }

  //print out what data are stored in the arrayList code_course
  //formerly toString, but right now it is not a standalone class object
  public String getAllSchedules() {
    String temp = "";
    for(String c: code_course.keySet()) {
      temp += "\n\n\nCourse: " + c + "\n";
      //code_course.get(c) returns an ArrayList
      temp += code_course.get(c).toString();
    }
    temp += "\n";
    return temp;

  }

  public ArrayList<CourseObject> getCalendar(){
    ArrayList<CourseObject> result = new ArrayList<CourseObject>();
    for (String s:code_course.keySet()) {
      result.addAll(code_course.get(s));
    }

    return result;
  }





  //put the building code in one variable alone











}