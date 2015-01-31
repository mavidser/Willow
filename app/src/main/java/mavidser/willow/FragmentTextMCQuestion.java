package mavidser.willow;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class FragmentTextMCQuestion extends Fragment {
    View myView;
    String q,a,b,c,d;

    public void changeThings(Bundle wow) {
        q = wow.getString("question");
        a = wow.getString("ad");
        b = wow.getString("bd");
        c = wow.getString("cd");
        d = wow.getString("dd");
        TextView ques = (TextView)myView.findViewById(R.id.text_question);
        TextView at = (TextView)myView.findViewById(R.id.text_a);
        TextView bt = (TextView)myView.findViewById(R.id.text_b);
        TextView ct = (TextView)myView.findViewById(R.id.text_c);
        TextView dt = (TextView)myView.findViewById(R.id.text_d);
        at.setText(a);
        bt.setText(b);
        ct.setText(c);
        dt.setText(d);

        ques.setText(q);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.activity_fragment_text_mcquestion, container, false);
        if (getArguments() != null) {
            changeThings(getArguments());
        }
        return myView;

    }

    public void setUIArguments(final Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
            /* do your UI stuffs */
                changeThings(args);
            }
        });
    }


//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_fragment_image_question, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
