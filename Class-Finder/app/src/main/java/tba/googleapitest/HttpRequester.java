package tba.googleapitest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by xCardo on 2/23/2018.
 */
public class HttpRequester {

    private static String serverAddr = "http://165.227.34.218";

    public static String getRoute(final String key, final MapsActivity context, List<String> bcodes, List<String> times) {
        //Retrieve list of building codes & times here
//        List<String> bcodes = new ArrayList<String>();
//        bcodes.add("BA");
//        bcodes.add("BR");
//        bcodes.add("PB");
//        List<String> times = new ArrayList<String>();
//        String dateFormat = "yyyy-MM-dd HH:mm";
//        times.add(convertToUTC("2018-03-07 09:00", dateFormat));
//        times.add(convertToUTC("2018-03-07 10:00", dateFormat));
//        times.add(convertToUTC("2018-03-07 11:00", dateFormat));

        List<String> convertedTimes = new ArrayList<String>();
        for (String time : times) {
            String dateFormat = "yyyy-MM-dd HHmm";
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String toBeConverted = String.format("%s-%s-%s %s", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), time);
            convertedTimes.add(convertToUTC(toBeConverted, dateFormat));
        }

        sendBuildingRequest(key, context, bcodes, convertedTimes, new ArrayList<String>());

        return "";
    }

    private static void sendBuildingRequest(final String key, final MapsActivity context, final List<String> bcodes, final List<String> times, final List<String> addresses) {
        if (bcodes.isEmpty()) {
            //send request to build and get json
            String addrStr= addresses.toString();
            //Toast.makeText(context, "Addresses: " + addrStr, Toast.LENGTH_LONG).show();
            Log.i("Addr",  "Addresses: "+addrStr);
            JSONObject reqObj = null;
            try {
                reqObj = new JSONObject();
                reqObj.put("route_id", 1);
                JSONArray paths = new JSONArray();
                if (addresses.size() <= 1) {
                    Log.e("Invalid params", "Addresses list not long enough, addresses="+addrStr);
                    return;
                }
                for (int i = 0; i < addresses.size() - 1; i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("origin", addresses.get(i));
                    obj.put("destination", addresses.get(i + 1));
                    obj.put("desired_arrival_time", times.get(i));
                    obj.put("mode", "walking");
                    paths.put(obj);
                }
                reqObj.put("paths", paths);
            } catch (JSONException e) {
                Log.e("JSONException", "Failed to build json for addresses="+addrStr);
                return;
            }
            Log.i("Req obj", reqObj == null ? "null" : reqObj.toString());
            String reqUrl = serverAddr + "/route";
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, reqUrl, reqObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //Toast.makeText(context, "Route Response is: " + response, Toast.LENGTH_LONG).show();
                    Log.i("Route response", "response="+response);

                    //convert response to list of polylines
                    List<String> polyLineList = convertResponse(response);
                    Log.i("Polylines", polyLineList.toString());

                    //Toast.makeText(context, "Polylines: " + polyLineList.toString(), Toast.LENGTH_LONG).show();

                    //pass list to activity method
                    context.requestCallback(key, polyLineList);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "That didn't work! ROUTE", Toast.LENGTH_LONG).show();
                    Log.e("route error", error.toString());
                }
            });
            queue.add(jsonReq);

        } else {
            String bcode = bcodes.get(0);
            bcodes.remove(0);
            String reqUrl = serverAddr + "/buildings/" + bcode;
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest strReq = new StringRequest(Request.Method.GET, reqUrl, new Response.Listener<String>() {
                public void onResponse(String response) {
                    try {
                        JSONArray objArr = new JSONArray(response);
                        JSONObject obj = objArr.getJSONObject(0);
                        Log.i("Obj", "Object="+obj.toString());
                        String addr = (String) obj.get("address");
                        addresses.add(addr);
                        sendBuildingRequest(key, context, bcodes, times, addresses);
                    } catch (JSONException e) {
                        Log.e("JSONException", "Failed to parse response="+response);
                        return;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "That didn't work! BUILDINGS", Toast.LENGTH_LONG).show();
                    Log.e("buildings error", error.toString());
                }
            });
            queue.add(strReq);
        }
    }

    private static List<String> convertResponse (JSONObject response) {
        List<String> rtn = new ArrayList<String>();
        try {
            JSONArray responseArr = response.getJSONArray("response");
            for (int i=0; i<responseArr.length(); i++) {
                JSONObject arrObj = responseArr.getJSONObject(i);
                JSONArray routes = arrObj.getJSONArray("routes");
                for (int j=0; j<routes.length(); j++) {
                    JSONObject route = routes.getJSONObject(j);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String polyline = overviewPolyline.getString("points");
                    rtn.add(polyline);
                }
            }
            return rtn;
        } catch (JSONException e) {
            Log.e("JSONException", "Failed to parse response="+response);
            return null;
        }

    }

    private static String convertToUTC(String date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date d = sdf.parse(date);
            Long epoch = d.getTime();
            return epoch.toString();
        } catch (ParseException e) {
            return null;
        }
    }

    public static void requestPathFromLocToClass(final String key, final MapsActivity context, final String latLng, final String bcode) {
        String reqUrl = serverAddr + "/buildings/" + bcode;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest strReq = new StringRequest(Request.Method.GET, reqUrl, new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    JSONArray objArr = new JSONArray(response);
                    JSONObject obj = objArr.getJSONObject(0);
                    Log.i("Obj", "Object="+obj.toString());
                    String addr = (String) obj.get("address");


                    JSONObject reqObj = null;
                    try {
                        reqObj = new JSONObject();
                        reqObj.put("route_id", 1);
                        JSONArray paths = new JSONArray();
                        JSONObject jobj = new JSONObject();
                        jobj.put("origin", latLng);
                        jobj.put("destination", addr);
                        jobj.put("desired_arrival_time", 0);
                        jobj.put("mode", "walking");
                        paths.put(jobj);

                        reqObj.put("paths", paths);
                    } catch (JSONException e) {
                        Log.e("JSONException", "Failed to build json for addresses="+addr);
                        return;
                    }


                    String reqUrl = serverAddr + "/route";
                    RequestQueue queue = Volley.newRequestQueue(context);
                    JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, reqUrl, reqObj, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(context, "Route Response is: " + response, Toast.LENGTH_LONG).show();
                            Log.i("Route response", "response="+response);

                            //convert response to list of polylines
                            List<String> polyLineList = convertResponse(response);
                            Log.i("Polylines", polyLineList.toString());

                            //Toast.makeText(context, "Polylines: " + polyLineList.toString(), Toast.LENGTH_LONG).show();

                            //pass list to activity method
                            context.requestCallback(key, polyLineList);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "That didn't work! ROUTE", Toast.LENGTH_LONG).show();
                            Log.e("route error", error.toString());
                        }
                    });
                    queue.add(jsonReq);

                } catch (JSONException e) {
                    Log.e("JSONException", "Failed to parse response="+response);
                    return;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "That didn't work! Loc building", Toast.LENGTH_LONG).show();
                Log.e("loc building error", error.toString());
            }
        });
        queue.add(strReq);
    }

}
