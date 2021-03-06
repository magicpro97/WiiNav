package com.linh.wiinav.modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.linh.wiinav.models.Distance;
import com.linh.wiinav.models.Duration;
import com.linh.wiinav.models.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DirectionFinder {
    private static final String TAG = "DirectionFinder";
    private static final String GOOGLE_DIRECTION_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyCTWFhXTvgVhGw-kHjV-wDWPTMsFENGzdg";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;

    public DirectionFinder(DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        Log.i(TAG, "execute: " + createUrl());
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        StringBuilder sb = new StringBuilder();
        sb.append(GOOGLE_DIRECTION_API);
        sb.append("origin=");
        sb.append(urlOrigin);
        sb.append("&destination=");
        sb.append(urlDestination);
        sb.append("&alternatives=true");
        sb.append("&key=");
        sb.append(GOOGLE_API_KEY);

        return sb.toString();
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            InputStream is;
            try {
                URL url = new URL(link);
                is = url.openConnection().getInputStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                is.close();
                return sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overviewPolylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);

            JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
            List<LatLng> intersectionCoordinates = new ArrayList<>();
            for (int j = 0; j < jsonSteps.length(); j++) {
                JSONObject jsonStep = jsonSteps.getJSONObject(j);
                JSONObject jsonInteractionCoordinate = jsonStep.getJSONObject("start_location");
                intersectionCoordinates.add(new LatLng(jsonInteractionCoordinate.getDouble("lat"),
                        jsonInteractionCoordinate.getDouble("lng")));
            }
            JSONObject jsonStep = jsonSteps.getJSONObject(jsonSteps.length() - 1);
            JSONObject jsonInteractionCoordinate = jsonStep.getJSONObject("end_location");
            intersectionCoordinates.add(new LatLng(jsonInteractionCoordinate.getDouble("lat"),
                    jsonInteractionCoordinate.getDouble("lng")));
            route.setIntersectionCoordinate(intersectionCoordinates);

            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");


            route.setDistance(new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value")));
            route.setDuration(new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value")));
            route.setEndAddress(jsonLeg.getString("end_address"));
            route.setStartAddress(jsonLeg.getString("start_address"));
            route.setStartLocation(new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng")));;
            route.setEndLocation(new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng")));
            route.setPoints(decodePolyLine(overviewPolylineJson.getString("points")));

            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes);
}

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
