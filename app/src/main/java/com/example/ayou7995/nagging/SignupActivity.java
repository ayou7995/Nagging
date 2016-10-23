package com.example.ayou7995.nagging;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

/**
 * Created by ayou7995 on 2016/10/19.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String Tag = "Jonathan";
    private static final String REGISTER = "register";

    EditText _nameText;
    // EditText _addressText;
    // EditText _emailText;
    // EditText _mobileText;
    EditText _passwordText;
    EditText _reEnterPasswordText;
    Button _signupButton;
    TextView _loginLink;
    String name = "";
    String password = "";
    String reEnterPassword = "";
    String stream = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        _nameText = (EditText) findViewById(R.id.input_name);
        // _addressText = (EditText) findViewById(R.id.input_address);
        // _emailText = (EditText) findViewById(R.id.input_email);
        // _mobileText = (EditText) findViewById(R.id.input_mobile);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        _signupButton = (Button) findViewById(R.id.btn_signup);
        _loginLink = (TextView) findViewById(R.id.link_login);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void signup() {
        Log.d(Tag, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        name = _nameText.getText().toString();
        // String address = _addressText.getText().toString();
        // String email = _emailText.getText().toString();
        // String mobile = _mobileText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();

        // TODO: Implement your own signup logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        tryLogin();
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        // String address = _addressText.getText().toString();
        // String email = _emailText.getText().toString();
        // String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }


        /*if (address.isEmpty()) {
            _addressText.setError("Enter Valid Address");
            valid = false;
        } else {
            _addressText.setError(null);
        }*/


        /*if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }*/

        /*if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }*/

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    public void tryLogin(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String urlString = "http://140.112.18.196:4000/login";
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
            Log.i(Tag, "Stream = " + stream);
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
                authentication.put("state", REGISTER);
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