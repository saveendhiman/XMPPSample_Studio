package com.example.xmppsample;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xmpp.chat.data.AppSettings;
import com.xmpp.chat.service.LiveAppService;
import com.xmpp.chat.xmpp.XMPP;

public class Activity_ListUser extends Activity implements OnClickListener {

    Context context;
    LinearLayout llNameMain1, llNameMain2;
    int value_director = 0;
    String dirName, dirId;
    Activity activity;
    boolean itsNewUser = false;
    private UserLoginTask mAuthTask = null;
    EditText etuserid, etNameuser1, etNameuser2, etHostName, etServerName;
    String userid;
    Button btnloginxmpp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userslist);
        context = Activity_ListUser.this;
        activity = Activity_ListUser.this;
        init();
        etHostName.setText("192.168.0.233");
        etServerName.setText("192.168.0.233");
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void init() {

        btnloginxmpp = (Button) findViewById(R.id.btnloginxmpp);
        etuserid = (EditText) findViewById(R.id.etuserid);
        etHostName = (EditText) findViewById(R.id.etHostName);
        etServerName = (EditText) findViewById(R.id.etServerName);

        etNameuser1 = (EditText) findViewById(R.id.etNameuser1);
        etNameuser2 = (EditText) findViewById(R.id.etNameuser2);
        llNameMain1 = (LinearLayout) findViewById(R.id.llNameMain1);
        llNameMain2 = (LinearLayout) findViewById(R.id.llNameMain2);

        llNameMain1.setOnClickListener(this);
        llNameMain2.setOnClickListener(this);
        btnloginxmpp.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private boolean register(String paramString1, String paramString2) {
        try {
            XMPP.getInstance().register(paramString1, paramString2);
            return true;
        } catch (XMPPException localXMPPException) {
            localXMPPException.printStackTrace();
        } catch (NoResponseException e) {
            e.printStackTrace();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean login(String user, String pass, String username) {
        try {
            AppSettings.setUser(this, user);
            AppSettings.setPassword(this, pass);
            AppSettings.setUserName(this, username);

            XMPP.getInstance().login(user, pass, AppSettings.getStatus(this),
                    username);
            sendBroadcast(new Intent("liveapp.loggedin"));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                AppSettings.setUser(this, user);
                AppSettings.setPassword(this, pass);
                AppSettings.setUserName(this, username);
                XMPP.getInstance().login(user + "@" + XMPP.HOST, pass,
                        AppSettings.getStatus(this), username);
                sendBroadcast(new Intent("liveapp.loggedin"));

                return true;
            } catch (XMPPException e1) {
                e1.printStackTrace();
            } catch (SaslException e1) {
                e1.printStackTrace();
            } catch (SmackException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return false;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        public UserLoginTask() {
        }

        protected Boolean doInBackground(Void... paramVarArgs) {

            String mEmail = userid;
            String mUsername = "user1";
            String mPassword = "welcome";

            if (register(userid, "welcome")) {
                try {
                    XMPP.getInstance().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return login(mEmail, mPassword, mUsername);
        }

        protected void onCancelled() {
            mAuthTask = null;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected void onPostExecute(Boolean success) {
            mAuthTask = null;

            if (success) {

                Toast.makeText(context,
                        "You successfully login,now you can start chat!",
                        Toast.LENGTH_LONG).show();

                startService(new Intent(activity, LiveAppService.class));
            } else {

            }
        }
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {

            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.llNameMain1:

                if (etNameuser1.getText().toString().equals("")) {

                    Toast.makeText(context, "Please enter user id",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent in = new Intent(activity, ActivityChatScreen.class);
                    in.putExtra("username", "user1");
                    in.putExtra("id", etNameuser1.getText().toString() + "@"
                            + XMPP.HOST + "/Smack");
                    startActivity(in);
                }

                break;
            case R.id.llNameMain2:
                if (etNameuser2.getText().toString().equals("")) {

                    Toast.makeText(context, "Please enter user id",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent in1 = new Intent(activity, ActivityChatScreen.class);
                    in1.putExtra("username", "user2");
                    in1.putExtra("id", etNameuser2.getText().toString() + "@"
                            + XMPP.HOST + "/Smack");
                    startActivity(in1);
                }

                break;
            case R.id.btnloginxmpp:
                if (etuserid.getText().toString().equals("")) {

                    Toast.makeText(context, "Please enter user id",
                            Toast.LENGTH_SHORT).show();
                } else {

                    userid = etuserid.getText().toString();
                    XMPP.HOST1 = etHostName.getText().toString();
                    XMPP.HOST = etServerName.getText().toString();

                    attemptLogin();
                }

                break;

            default:
                break;
        }
    }

}
