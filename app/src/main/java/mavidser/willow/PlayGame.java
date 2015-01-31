package mavidser.willow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayGame extends ActionBarActivity {
    String username,match;
    int level,CURRENT_QUESTION=-1,xp=0,opponent_xp=0;
    ProgressDialog pd;
    FragmentTransaction ft;
    FragmentImageQuestion iFragment = new FragmentImageQuestion();
    FragmentTextMCQuestion tFragment = new FragmentTextMCQuestion();
    GameResult rFragment = new GameResult();
    JSONArray obj;


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.150.2:4000/");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        username = intent.getStringExtra("userid");
        level = intent.getIntExtra("level",1);

        mSocket.connect();
        System.out.println(username);
        mSocket.emit("connectUser", username);
            mSocket.emit("questions", "test");
            mSocket.on("message", response);
        getMatch();
        mSocket.on("receiveMatchingUser", matchReceived);
        mSocket.on("userDisconnected", userDisconnected);
        setContentView(R.layout.activity_play_game);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
//        mSocket.emit("haha", username);
        super.onDestroy();
//        mSocket.emit("haha", username);
        mSocket.disconnect();
        mSocket.off("receiveMatchingUser", matchReceived);
        mSocket.off("userDisconnected",userDisconnected);
    }

    private void getMatch() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("userid", username);
            obj.put("level", level);
        } catch (Exception e) {}
        mSocket.emit("matchUser", obj);

        pd=new ProgressDialog(this);

        pd.setIndeterminate(true);
//        pd.setProgressStyle(pd.STYLE_HORIZONTAL);
//        pd.setMax(100);
//        pd.setProgress(0);
        pd.setMessage("Waiting for a match");
        pd.setCancelable(true);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
//                onBackPressed();
                pd.cancel();
            }
        });

try {        pd.show();}catch (Exception e) {}
    }

    private Emitter.Listener matchReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String user = (String) args[0];
                    match = user;
                    TextView opponent = (TextView) findViewById(R.id.play_opponent);
                    TextView user_ = (TextView) findViewById(R.id.play_user);
                    user_.setText(username);
                    opponent.setText(match);
                    try {
                        pd.dismiss();
                    } catch (Exception e) {}
                }
            });
//            mSocket.emit("questions", "test");
//            mSocket.on("message", response);
        }
    };

    private Emitter.Listener userDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), "Your opponent left the game",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Emitter.Listener response = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

//                    System.out.println("HI");
                    try {
//                        int i=0;
                        String json = (String)args[0];
                        obj = new JSONArray(json);
//                        for(int i=0;i<obj.length();i++) {
//                            Bundle data = new Bundle();
//                        System.out.println("THE QUESTSION IS "+obj.getJSONObject(CURRENT_QUESTION).getString("q"));
//                            data.putString("question", obj.getJSONObject(CURRENT_QUESTION).getString("q"));
//                            data.putString("ad", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").getString("desc"));
//                            data.putString("bd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("b").getString("desc"));
//                            data.putString("cd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("c").getString("desc"));
//                            data.putString("dd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("d").getString("desc"));
//                            try {
//                                data.putString("ai", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").getString("url"));
//                                data.putString("bi", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("b").getString("url"));
//                                data.putString("ci", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("c").getString("url"));
//                                data.putString("di", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("d").getString("url"));
//                            }catch (Exception e){}
//                            iFragment.setArguments(data);
                            answer(null);
//                        }
                    } catch (JSONException e) {


                        System.err.println(e);

                        return;
                    }
                }
            });
        }
    };

    public void putQuestionInFragment(int i) {
        System.out.println("HIHI");
        ft = getSupportFragmentManager().beginTransaction();

        try {
            obj.getJSONObject(i).getJSONObject("a").get("url");

            ft.replace(R.id.content_frame, iFragment);
        } catch (Exception e) {
            ft.replace(R.id.content_frame, tFragment);
        }
        ft.disallowAddToBackStack();
        ft.commit();
    }
//
    public void answer(View view) {
//        System.out.println("IJFLJLFKFLK");

        Bundle data = new Bundle();
        try{
            data.putString("question", obj.getJSONObject(++CURRENT_QUESTION).getString("q")); data.putString("question", obj.getJSONObject(CURRENT_QUESTION).getString("q"));
            data.putString("ad", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").getString("desc"));
            data.putString("bd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("b").getString("desc"));
            data.putString("cd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("c").getString("desc"));
            data.putString("dd", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("d").getString("desc"));
            try {
                data.putString("ai", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").getString("url"));
                data.putString("bi", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("b").getString("url"));
                data.putString("ci", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("c").getString("url"));
                data.putString("di", obj.getJSONObject(CURRENT_QUESTION).getJSONObject("d").getString("url"));
            }catch (Exception e){}


            try {
                obj.getJSONObject(CURRENT_QUESTION).getJSONObject("a").get("url");

                try {iFragment.setArguments(data);}
                catch(Exception ce){iFragment.setUIArguments(data);}

            } catch (Exception e) {

//            try {
//                ft.remove(iFragment);
//            } catch (Exception xe) {}

                try {tFragment.setArguments(data);}
                catch(Exception ce){tFragment.setUIArguments(data);}

            }

//            iFragment.setArguments(data);
//            tFragment.setArguments(data);
            putQuestionInFragment(CURRENT_QUESTION);
        } catch (Exception e) {
            System.err.println(e);

            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, rFragment);
            ft.disallowAddToBackStack();
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
