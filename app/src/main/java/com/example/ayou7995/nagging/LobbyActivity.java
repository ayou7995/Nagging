package com.example.ayou7995.nagging;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {

    private final static String Tag = "Jonathan";
    private final static String urlString = "http://140.112.18.196:4000/lobby";
    private final static String NAME = "username";
    private final static String STATE = "state";
    private final static String NAGREQUEST = "nag_request";

    Button init_button;
    Button refresh_button;
    Button chat_button;
    ListView listview;
    List<LobbyRow> lobbyList;
    private LobbyAdapter lobbyAdapter;
    private Handler handler;

    private String user_name = "";
    // private String user_state = "";
    // private String user_nag_request = "";
    private String stream = "";
    private JSONObject jsonObject;

    boolean toNag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Initialize widgets
        listview = (ListView) findViewById(R.id.lobby_lv);
        init_button = (Button) findViewById(R.id.initialize_btn);
        refresh_button = (Button) findViewById(R.id.refresh_btn);
        chat_button = (Button) findViewById(R.id.chat_btn);
        init_button.setOnClickListener(clickHandler);
        refresh_button.setOnClickListener(clickHandler);
        chat_button.setOnClickListener(clickHandler);

        // Initialize jsonObject
        jsonObject = new JSONObject();
        if(!getIntent().getExtras().isEmpty()) {
            user_name = getIntent().getExtras().getString("user_name");
        }
        // user_state = "online";
        // user_nag_request = "";

        // Lobby list View related
        lobbyList = new ArrayList<>();
        lobbyAdapter = new LobbyAdapter(this, lobbyList);
        listview.setAdapter(lobbyAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // jump2ChatRoom(user_name, ((LobbyRow)adapterView.getItemAtPosition(i)).get_name());
                String connectSubject = ((LobbyRow)adapterView.getItemAtPosition(i)).get_name();
                if(checkNetwork()) {
                    setJsonObject(user_name, "online", connectSubject);
                    new LobbyTask().execute(urlString);
                }
            }
        });

        // Continue to send jsonObject to server
        handler = new Handler();
        handler.removeCallbacks(sendData);
        handler.postDelayed(sendData, 5000);
    }

    View.OnClickListener clickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                /*case R.id.refresh_btn:
                    lobbyAdapter.notifyDataSetChanged();
                    break;*/
                /*case R.id.initialize_btn:
                    for(int i=0 ; i<10 ; ++i) {
                        LobbyRow lobbyRow = new LobbyRow();
                        lobbyRow.set_name("Jonathan");
                        lobbyRow.set_state("busy");
                        lobbyRow.set_subject("Bo");
                        lobbyList.add(lobbyRow);
                    }
                    lobbyAdapter.notifyDataSetChanged();
                    break;*/
                /*case R.id.chat_btn:
                    Intent intent = new Intent();
                    intent.setClass(LobbyActivity.this, ChatRoomActivity.class);
                    startActivity(intent);
                    finish();
                    break;*/
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        toNag = false;
        if(checkNetwork()) {
            handler.removeCallbacks(sendData);
            handler.postDelayed(sendData, 5000);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(checkNetwork()) {
            if(!toNag) {
                setJsonObject(user_name, "leave", "");
                new LobbyTask().execute(urlString);
            }
        }
        handler.removeCallbacks(sendData);
    }

    private void setJsonObject(String name, String state, String nagrequest) {
        jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME, name);
            jsonObject.put(STATE, state);
            jsonObject.put(NAGREQUEST, nagrequest);
            Log.i(Tag, "jsonObject: " + jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(Tag, "Catch : Fail to turn user's name, state, and nagRequest to JSON object");
        }
    }


    private Runnable sendData = new Runnable() {
        @Override
        public void run() {
            if(checkNetwork()) {
                setJsonObject(user_name, "online", "");
                new LobbyTask().execute(urlString);
            }
            handler.postDelayed(this,5000);
        }
    };

    private boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            Log.i(Tag, "No network connection available");
            return false;
        }
    }

    private class LobbyTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.i(Tag, "URLS = " + urls[0]);
            try {
                return connectServer(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(Tag,"Unable to connect to server.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            lobbyAdapter.notifyDataSetChanged();
        }
    }

    private String connectServer(String urls) throws IOException {

        HttpURLConnection conn = null;

        try {
            // HttpURLConnection
            URL url = new URL(urls);
            Log.i(Tag, "HttpURLConnection established.");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000); // milliseconds
            conn.connect();

            // Write jsonObject to server
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(jsonObject.toString());
            wr.flush();
            wr.close();

            // Receive jsonObject back from server
            InputStreamReader is = new InputStreamReader(conn.getInputStream());
            BufferedReader reader = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }reader.close();
            is.close();

            // Get response code
            int response = conn.getResponseCode();
            Log.d(Tag, "The response is: " + response);

            // Decode jsonString ...
            decodeJSON(sb.toString());

            return "Success";
        } catch (MalformedURLException e) {
            Log.i(Tag, "bAD");
            return "bad";
            //do whatever you want to do if you get the exception here
        } finally {
            assert conn != null;
            conn.disconnect();
        }
    }

    public class LobbyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<LobbyRow> lobbyRowList;

        public LobbyAdapter(Context context, List<LobbyRow> list) {
            mInflater = LayoutInflater.from(context);
            lobbyRowList = list;
        }
        @Override
        public int getCount() {
            return lobbyRowList.size();
        }

        @Override
        public Object getItem(int i) {
            return lobbyRowList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            Log.i(Tag, "LobbyAdapter.getView Called. Position: " + String.valueOf(position));
            ViewHolder viewHolder;

            LobbyRow lobbyRow = (LobbyRow) getItem(position);

            if(convertView == null) {
                Log.d(Tag, "ConvertView == null");
                convertView = mInflater.inflate(R.layout.lobby_row, null);
                viewHolder = new ViewHolder();
                viewHolder.user_portrait = (ImageView) convertView.findViewById(R.id.user_portrait);
                viewHolder.user_name = (TextView) convertView.findViewById(R.id.user_name);
                viewHolder.user_state = (TextView) convertView.findViewById(R.id.user_state);
                viewHolder.user_subject = (TextView) convertView.findViewById(R.id.user_subject);
                convertView.setTag(viewHolder);
            } else {
                Log.d(Tag, "ConvertView != null");
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.user_name.setText(lobbyRow.get_name());
            viewHolder.user_state.setText(lobbyRow.get_state());
            viewHolder.user_subject.setText(lobbyRow.get_subject());

            return convertView;
        }

        private class ViewHolder {
            ImageView user_portrait;
            TextView user_name;
            TextView user_state;
            TextView user_subject;
        }
    }

    private void jump2ChatRoom(String user1, String user2){
        Log.i(Tag,"Username (" + user1 + ") starts to nag with "+user2);
        handler.removeCallbacks(sendData);
        toNag = true;
        Intent intent = new Intent();
        intent.setClass(LobbyActivity.this, ChatRoomActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_name", user1);
        bundle.putString("subject_name", user2);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void decodeJSON(String jsonStr) {
        JSONObject jsonResponse;
        Log.i(Tag,jsonStr);
        try {
            // List<String> msgList = new ArrayList<>();
            String empty;
            jsonResponse = new JSONObject(jsonStr);
            JSONArray jsonList = new JSONArray();
            String msgState = jsonResponse.getString("message");
            if(jsonResponse.get("userlist") instanceof JSONArray) {
                jsonList = jsonResponse.getJSONArray("userlist"); // msg_list = [];
            } else {
                empty = jsonResponse.getString("userlist");
            }
            if(msgState.compareTo("unchange")==0) {
                if(jsonList!=null) {
                    lobbyList.clear();
                    for (int i = 0; i < jsonList.length(); ++i) {
                        JSONObject jsonObject = jsonList.getJSONObject(i);
                        LobbyRow lobbyRow = new LobbyRow();
                        lobbyRow.set_name(jsonObject.getString("Name"));
                        lobbyRow.set_subject(jsonObject.getString("Guest"));
                        lobbyRow.set_state(jsonObject.getString("State"));
                        lobbyList.add(lobbyRow);
                    }
                }
            } else if (msgState.compareTo("connect")==0) {
                if(jsonList!=null){
                    JSONObject jsonObject = jsonList.getJSONObject(0);
                    if(jsonObject.getString("Guest").compareTo(user_name)==0) {
                        jump2ChatRoom(user_name, jsonObject.getString("Name"));
                    }
                }
            } else {
                Log.i(Tag, "msgState = " + msgState);
            }

        } catch (JSONException e) {
            Log.i(Tag, "Unable to decode JSON string");
            e.printStackTrace();
        }
    }
}
