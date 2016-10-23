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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    // Static String
    private static final String Tag = "Jonathan";
    private final static String urlString = "http://140.112.18.196:4000/nagroom";
    private final static String USERNAME = "user1";     // {"user1"="sender"}
    private final static String SUBJECTNAME = "user2";  // {"user2"="receiver"}
    private final static String STATE = "state";        // {"state"="online"||"leave"}
    private final static String MESSAGE = "message";    // {"message"=""||"type_message"}

    // Widgets
    private EditText msg_et;
    private ListView msg_lv;
    private Button send_btn;

    // List view related
    List<ChatMessage> msg_list;
    MessageListAdapter msg_Adapter;

    // Server related objects
    private JSONObject jsonObject;
    private Handler handler;
    private String user_name = "";
    private String subject_name = "";
    private String user_state = "";
    private String message = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // Initialize widgets and list_view
        msg_lv = (ListView) findViewById(R.id.msgview);
        msg_et = (EditText) findViewById(R.id.msg_et);
        send_btn = (Button) findViewById(R.id.send_btn);

        msg_list = new ArrayList<>();
        msg_Adapter = new MessageListAdapter(ChatRoomActivity.this, msg_list);
        msg_lv.setAdapter(msg_Adapter);

        // Initialize jsonObject and send join chat room message to server.
        jsonObject = new JSONObject();
        if(!getIntent().getExtras().isEmpty()) {
            user_name = getIntent().getExtras().getString("user_name");
            subject_name = getIntent().getExtras().getString("subject_name");
        }
        if(checkNetwork()) {
            setJsonObject(user_name, subject_name, "join", "");
            new ChatRoomTask().execute(urlString);
        }

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = msg_et.getText().toString();
                if(message.compareTo("")!=0){
                    // Todo
                    Log.i(Tag, "Send button onClicked");
                    setJsonObject(user_name, subject_name, "nag", message);
                    new ChatRoomTask().execute(urlString);
                }
                /* for(int i=0 ; i<10 ; ++i){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setLeft(i % 2 == 0);
                    chatMessage.setMessage((i%2==0)?"Hello! My name is Jonathan. Nice to meet you."
                            :"Hi. My name is Bo. Nice to meet you too.");
                    msg_list.add(chatMessage);
                }
                msg_Adapter.notifyDataSetChanged(); */
            }
        });

        // Continue to send connect message to server
        handler = new Handler();
        handler.removeCallbacks(sendData);
        handler.postDelayed(sendData, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(checkNetwork()) {
            setJsonObject(user_name, subject_name, "leave", "");
            new ChatRoomTask().execute(urlString);
        }
        handler.removeCallbacks(sendData);
    }

    private void setJsonObject(String user1, String user2, String state, String message) {
        jsonObject = new JSONObject();
        try {
            jsonObject.put(USERNAME, user1);
            jsonObject.put(SUBJECTNAME, user2);
            jsonObject.put(STATE, state);
            jsonObject.put(MESSAGE, message);
            Log.i(Tag, "jsonObject: " + jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(Tag, "Catch : Fail to turn user's name, state, and nagRequest to JSON object");
        }
    }

    private Runnable sendData = new Runnable() {
        @Override
        public void run() {
            if(checkNetwork()){
                setJsonObject(user_name, subject_name, "wait", "");
                new ChatRoomTask().execute(urlString);
            }
            handler.postDelayed(this,1000);
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

    private class ChatRoomTask extends AsyncTask<String, Void, String> {

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
            }
            reader.close();
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

    public class MessageListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<ChatMessage> msg_list;

        public MessageListAdapter(Context context, List<ChatMessage> list){
            this.inflater = LayoutInflater.from(context);
            this.msg_list = list;
        }

        @Override
        public int getCount() {
            return msg_list.size();
        }

        @Override
        public Object getItem(int i) {
            return msg_list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Log.i(Tag, "LobbyAdapter.getView Called. Position: " + String.valueOf(position));
            // LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            ViewHolder viewHolder;
            ChatMessage chat_msg = (ChatMessage) getItem(position);

            if(convertView==null) {
                Log.d(Tag, "ConvertView == null");
                viewHolder = new ViewHolder();
                if (chat_msg.left) {
                    convertView = inflater.inflate(R.layout.right, parent, false);
                } else {
                    convertView = inflater.inflate(R.layout.left, parent, false);
                }
                viewHolder.msgText = (TextView) convertView.findViewById(R.id.msgr);
                convertView.setTag(viewHolder);
            } else {
                Log.d(Tag, "ConvertView != null");
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.msgText.setText(chat_msg.message);

            return convertView;
        }

        private class ViewHolder {
            TextView msgText;
        }
    }

    private void decodeJSON(String jsonStr) {
        JSONObject jsonResponse;
        Log.i(Tag,jsonStr);
        try {
            // List<String> msgList = new ArrayList<>();
            jsonResponse = new JSONObject(jsonStr);
            JSONArray msgArray = jsonResponse.getJSONArray("msg_list"); // msg_list = [];
            for(int i=0 ; i<msgArray.length() ; ++i){
                JSONObject jsonObject = msgArray.getJSONObject(i);
                ChatMessage chatMessage = new ChatMessage();
                if(jsonObject.getString("sender").compareTo(user_name)==0) {
                    chatMessage.setLeft(false);
                } else {
                    chatMessage.setLeft(true);
                }
                chatMessage.setMessage(jsonObject.getString("message"));
                msg_list.add(chatMessage);
                msg_Adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
