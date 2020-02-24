package com.example.new_login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static String KEY_UID = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";

    private MaterialButton btnLogin, btnLinkToRegister, btnForgotPass;
    private TextInputLayout inputEmail, inputPassword;
    private ProgressDialog pDialog;

    private SessionManager session;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.lTextEmail);
        inputPassword = findViewById(R.id.lTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLinkToRegister = findViewById(R.id.btnLinkToRegisterScreen);
        btnForgotPass = findViewById(R.id.btnForgotPassword);

        //progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //create sqlite database
        db = new DatabaseHandler(getApplicationContext());

        //session manager
        session = new SessionManager(getApplicationContext());

        //check user is already logged in
        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();
    }

    private void init() {
        //login button click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide keyboard
                Functions.hideSoftKeyboard(LoginActivity.this);

                String email = inputEmail.getEditText().getText().toString().trim();
                String password = inputPassword.getEditText().getText().toString().trim();

                //check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    if (Functions.isValidEmailAddress(email)) {
                        //login user
                        loginProcess(email, password);
                    } else
                        Toast.makeText(getApplicationContext(), "email tidak valid", Toast.LENGTH_SHORT).show();
                } else {
                    //prompt user to enter credentials
                    Toast.makeText(getApplicationContext(), "Please enter the credentials", Toast.LENGTH_LONG).show();
                }
            }
        });

        //btn to register screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //forgot password dialog
        btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog();
            }
        });
    }

    private void forgotPasswordDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.reset_password, null);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("forgot password");
        dialogBuilder.setCancelable(false);

        final TextInputEditText mEditEmail = dialogView.findViewById(R.id.editEmail);

        dialogBuilder.setPositiveButton("reset", new DialogInterface.OnClickListener() {
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


//        mEditEmail.getEditText().addTextChangedListener(new TextWatcher() {
        mEditEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // missing getText() in here
                if (mEditEmail.getEditableText().length() > 0) {
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setEnabled(false);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = mEditEmail.getEditableText().toString();

                        if (!email.isEmpty()) {
                            if (Functions.isValidEmailAddress(email)) {
                                resetPassword(email);
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "email is not valid ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "fill all values!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void loginProcess(final String email, final String password) {
        //tag used to cancel request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response : " + response);
                hideDialog();

                try {
                    JSONObject JObj = new JSONObject(response);
                    boolean error = JObj.getBoolean("error");

                    //check for error node in json
                    if (!error) {
                        JSONObject json_user = JObj.getJSONObject("user");

                        Functions logout = new Functions();
                        logout.logoutUser(getApplicationContext());

                        if (Integer.parseInt(json_user.getString("verified")) == 1) {
                            db.addUser(json_user.getString(KEY_UID), json_user.getString(KEY_NAME), json_user.getString(KEY_EMAIL), json_user.getString(KEY_CREATED_AT));
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            session.setLogin(true);
                            finish();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("email", email);
                            Intent intent = new Intent(LoginActivity.this, EmailVerify.class);
                            intent.putExtras(bundle);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        }
                    } else {
                        //error in login.get the error message
                        String errorMsg = JObj.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    //json error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "json error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "login error :" + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;

            }

        };
        //adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void resetPassword(final String email){
        //TAG used to cancel the request
        String tag_string_req = "req_string_pass";

        pDialog.setMessage("please wait...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Functions.RESET_PASS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Reset password response : " + response);
                hideDialog();

                try {
                    JSONObject jobj = new JSONObject(response);
                    boolean error = jobj.getBoolean("error");

                    if (!error) {
                        Toast.makeText(getApplicationContext(), jobj.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), jobj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "reset password error :"+ error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();

                params.put("tag", "forgot_pass");
                params.put("email", email);

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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        MyApplication.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
