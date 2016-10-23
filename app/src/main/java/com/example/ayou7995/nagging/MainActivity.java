package com.example.ayou7995.nagging;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String Tag = "Jonathan";
    private static final String LOGIN = "login";
    private static final String urlString = "http://140.112.18.196:4000/login";
    EditText idText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;
    TextView showText;
    String name;
    String password;
    String stream = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idText = (EditText) findViewById(R.id.input_name);
        passwordText = (EditText) findViewById(R.id.input_password);
        loginButton = (Button) findViewById(R.id.btn_login);
        signupLink = (TextView) findViewById(R.id.link_signup);
        showText = (TextView) findViewById(R.id.showText);
        loginButton.setOnClickListener(myBtnListener);

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private View.OnClickListener myBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_login:
                    name = idText.getText().toString();
                    password = passwordText.getText().toString();
                    /*Intent intent = new Intent();
                    intent.setClass(MainActivity.this, LobbyActivity.class);
                    startActivity(intent);
                    finish();*/
                    tryLogin();
            }
        }
    };
    public void tryLogin(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i(Tag,"Url = " + urlString);
            new LoginTask().execute(urlString);
        } else {
            Log.i(Tag, "No network connection available");
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            Log.i(Tag, "URLS = " + urls[0]);
            try {
                return connectServer(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(Tag,"Unable to connect to server.");
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            showText.setText(stream);
        }
    }

    private String connectServer(String urls) throws IOException {

        InputStreamReader is = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urls);
            Log.i(Tag, "HttpURLConnection established.");

            conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000); // milliseconds
            conn.connect();

            Log.i(Tag, "good");
            JSONObject authentication = new JSONObject();
            try {
                authentication.put("state", LOGIN);
                authentication.put("name", name);
                authentication.put("password", password);
                Log.i(Tag, "Authentication: " + authentication.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(Tag, "Catch : Fail to turn id and password to JSON object");
            }
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(authentication.toString());
            // wr.write("{\"id\":\"" + id + "\",\"password\":\"" + password + "\"}");
            wr.flush();
            wr.close();

            is = new InputStreamReader(conn.getInputStream());
            BufferedReader reader = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            stream = sb.toString();
            Log.i(Tag,"Stream = " + stream);
            reader.close();
            is.close();

            /*JSONObject jObject = new JSONObject(stream);
            String _id = jObject.getString("id");
            String _password = jObject.getString("password");
            Log.i(Tag, "id = " + _id + ", password = " + _password);*/

            int response = conn.getResponseCode();
            Log.d(Tag, "The response is: " + response);

            if(stream.compareTo("Successfully Login")==0) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LobbyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("user_name", name);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            } else if (stream.compareTo("Wrong Password")==0) {
                Toast.makeText(MainActivity.this, "Wrong Password.", Toast.LENGTH_SHORT).show();
            } else if (stream.compareTo("Unregistered ID")==0) {
                Toast.makeText(MainActivity.this, "Name don't exist. Sign up for one?", Toast.LENGTH_SHORT).show();
            }

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
}
