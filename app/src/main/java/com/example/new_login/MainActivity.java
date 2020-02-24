package com.example.new_login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.new_login.helper.DatabaseHandler;
import com.example.new_login.helper.Functions;
import com.example.new_login.helper.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView txtName, txtEmail;
    private MaterialButton btnChange, btnLogout, btnmapbox;
    private SessionManager session;
    private DatabaseHandler db;

    private ProgressDialog progressDialog;
    private HashMap<String,String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnmapbox = findViewById(R.id.mapbox);

        txtName = findViewById(R.id.name);
        txtEmail = findViewById(R.id.email);
        btnChange = findViewById(R.id.change_password);
        btnLogout = findViewById(R.id.logout);

        //progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        db = new DatabaseHandler(getApplicationContext());
        progressDialog.setCancelable(false);

        //session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()){
            logoutUser();
        }

        //fetching user details from database
        String name = user.get("name");
        String email = user.get("email");

        //displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();
    }
    private void init() {
        btnmapbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapboxView();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.change_password, null);

                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle("change password");
                dialogBuilder.setCancelable(false);

                final TextInputLayout oldPassword = dialogView.findViewById(R.id.old_password);
                final TextInputLayout newPassword = dialogView.findViewById(R.id.new_password);

                dialogBuilder.setPositiveButton("change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //empty
                    }
                });
                dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                final AlertDialog alertDialog = dialogBuilder.create();

                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (oldPassword.getEditText().getText().length() > 0 &&
                                newPassword.getEditText().getText().length() > 0) {
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        } else {
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                };

                oldPassword.getEditText().addTextChangedListener(textWatcher);
                newPassword.getEditText().addTextChangedListener(textWatcher);

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setEnabled(false);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String email = user.get("email");
                                String old_pass = oldPassword.getEditText().getText().toString();
                                String new_pass = newPassword.getEditText().getText().toString();

                                if (!old_pass.isEmpty() && !new_pass.isEmpty()) {
                                    changePassword(email, old_pass, new_pass);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Fill all values!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });
    }
    private void logoutUser(){
        session.setLogin(false);
        //launching the login activity
        Functions logout = new Functions();
        logout.logoutUser(getApplicationContext());
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void mapboxView(){
        Intent intent = new Intent(MainActivity.this, mapBox.class);
        startActivity(intent);
        finish();
    }


    private void changePassword(final String email, final String old_pass, final String new_pass){
        //tag used to cancel the request
        String tag_string_req = "req_reset_pass";

        progressDialog.setMessage("please wait");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.RESET_PASS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "reset password response : " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Toast.makeText(getApplicationContext(), jObj.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), jObj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    //json error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "reset password error" + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();

                params.put("tag", "change_pass");
                params.put("email", email);
                params.put("old_password", old_pass);
                params.put("password", new_pass);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };
        // Adding request to volley request queue
        strReq.setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
        strReq.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
