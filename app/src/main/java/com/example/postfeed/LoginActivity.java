package com.example.postfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements RestAPIResponse {
    public static final int USERLOGIN_MOBILE = 0x01;
    TextInputEditText username,password;
    TextInputLayout usernameLayout,passwordLayout;
    Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=findViewById(R.id.username);
        usernameLayout=findViewById(R.id.username_text_field);
        password=findViewById(R.id.password);
        passwordLayout=findViewById(R.id.password_text_field);
        submit=findViewById(R.id.login);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().length()<3)
                {
                    usernameLayout.setError("Username should not  be less than 3 characters");
                }
               else if(password.getText().toString().length()<3)
                {
                    passwordLayout.setError("Password should not  be less than 3 characters");
                }
               else
                {
                    user_login();
                }
            }
        });

    }

    private void user_login() {
        MainActivity.hideKeyboard(this);
        showProgress();
        Log.i("🙏🏻","userlogin_mobile");
        try {

            URL url = new URL(Constants.SERVER_URL + "/users");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("name", Objects.requireNonNull(username.getText()).toString()));
            nameValuePairs.add(new BasicNameValuePair("password", Objects.requireNonNull(password.getText()).toString()));
            CallAPI callAPI = new CallAPI(url, nameValuePairs, USERLOGIN_MOBILE,this);
            callAPI.execute(this);

        } catch (Exception e) {
            Log.i("==errorallletbalance", "failed");
        }
    }

    private void showProgress() {
        findViewById(R.id.loginmainlayout).setVisibility(View.GONE);
        findViewById(R.id.pbHeaderProgresswallet).setVisibility(View.VISIBLE);
    }
     private void hideProgress() {
        findViewById(R.id.loginmainlayout).setVisibility(View.VISIBLE);
        findViewById(R.id.pbHeaderProgresswallet).setVisibility(View.GONE);
    }

    @Override
    public void postRestAPICall(String response, int callerid, CallApiResponse callApiResponse) {
        Log.i("🙏🏻",response);
        try {
            JSONObject json=new JSONObject(response);
            if(json.get("response") instanceof JSONArray)
            {
                JSONArray jsonArray=json.getJSONArray("response");
                Utils.setDefaults("userid",jsonArray.getJSONObject(0).getString("id"),LoginActivity.this);
                Intent i = new Intent(LoginActivity.this, PostsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
            else if(json.get("response") instanceof String)
            {
                if(json.getString("response").compareTo("success")==0)
                    {
                        user_login();
                    }
            }
            else
            {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}