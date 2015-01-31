package mavidser.willow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.AsyncTask;
//import com.google.android.gms.internal.os.AsyncTask;
//import com.google.android.gms.internal.os.Bundle;

import java.net.URL;


public class FragmentImageQuestion extends Fragment {
    View myView;
    String q,ad,ai,bd,bi,cd,ci,dd,di;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

//        setContentView(R.layout.activity_fragment_image_question);
    }

    public void changeThings(Bundle wow) {
        q = wow.getString("question");
        ad = wow.getString("ad");
        ai = wow.getString("ai");
        bd = wow.getString("bd");
        bi = wow.getString("bi");
        cd = wow.getString("cd");
        ci = wow.getString("ci");
        dd = wow.getString("dd");
        di = wow.getString("di");

        TextView ques = (TextView)myView.findViewById(R.id.image_question);
        TextView adt = (TextView)myView.findViewById(R.id.iad);
        adt.setText(ad);
        TextView bdt = (TextView)myView.findViewById(R.id.ibd);
        bdt.setText(bd);
        TextView cdt = (TextView)myView.findViewById(R.id.icd);
        cdt.setText(cd);
        TextView ddt = (TextView)myView.findViewById(R.id.idd);
        ddt.setText(dd);

        new RetrieveImages().execute("");




        ques.setText(q);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.activity_fragment_image_question, container, false);
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


    class RetrieveImages extends AsyncTask<String, Void, Integer> {

        private Exception exception;

        protected Integer doInBackground(String... urls) {
            try {

                final ImageButton aii = (ImageButton)myView.findViewById(R.id.iai);

                URL url = new URL("http://192.168.150.2:4000/images/" + ai);

                final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                aii.setImageBitmap(bmp);


                final ImageButton bii = (ImageButton) myView.findViewById(R.id.ibi);

                url = new URL("http://192.168.150.2:4000/images/" + bi);
                final Bitmap bmp2 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                bii.setImageBitmap(bmp);


                final ImageButton cii = (ImageButton) myView.findViewById(R.id.ici);

                url = new URL("http://192.168.150.2:4000/images/" + ci);
                final Bitmap bmp3 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                cii.setImageBitmap(bmp);


                final ImageButton dii = (ImageButton) myView.findViewById(R.id.idi);

                url = new URL("http://192.168.150.2:4000/images/" + di);
                final Bitmap bmp4 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                dii.setImageBitmap(bmp);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aii.destroyDrawingCache();
                        aii.setImageBitmap(bmp);
                        bii.destroyDrawingCache();
                        bii.setImageBitmap(bmp2);
                        cii.destroyDrawingCache();
                        cii.setImageBitmap(bmp3);
                        dii.destroyDrawingCache();
                        dii.setImageBitmap(bmp4);


                    }
                });

            } catch (Exception e) {
                this.exception = e;
                System.err.println(e);
                return 1;
            }
            return 0;
        }

        protected void onPostExecute(int feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
}
