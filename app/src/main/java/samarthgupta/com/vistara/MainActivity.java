package samarthgupta.com.vistara;

import android.content.Intent;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,BeaconManager.RangingListener, BeaconManager.MonitoringListener {

    private static final String TAG = "TAG";
    BeaconManager beaconManager;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    int prev=0, next=0;
    TextToSpeech textToSpeech;
    int countVolUp =0, countVolDown = 0;
    int nearestBeacon =0;
    String dialog = "";
    TextView tvTemp;

    private static final int SPEECH_REQUEST_CODE = 0;

    ImageView ivHelp, ivLoc, ivSpeech;


    public HashMap<Integer,Integer> bIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        tvBc = (TextView) findViewById(R.id.tv_bc);

        beaconManager = new BeaconManager(this);

        beaconManager.setRangingListener(this);
        beaconManager.setMonitoringListener(this);
        bIDs = new HashMap<>();
        bIDs.put(5,1);
        bIDs.put(20974,2);

        ivHelp = (ImageView) findViewById(R.id.iv_help);
        ivLoc = (ImageView) findViewById(R.id.iv_loc);
        ivSpeech = (ImageView) findViewById(R.id.iv_speech);
        ivSpeech.setOnClickListener(this);
        ivHelp.setOnClickListener(this);
        ivLoc.setOnClickListener(this);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        Log.i("Init","Initialised");
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });


        tvTemp = (TextView) findViewById(R.id.tv_temp);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS_REGION);
                Log.i("TAG","Service");
            }

        });
    }

    @Override
    public void onEnteredRegion(Region region, List<Beacon> list) {
        Log.i("TAG","Enter"+region.getMajor()+" "+list.size()+" "+list.get(0).getMajor());


    }

    @Override
    public void onExitedRegion(Region region) {
        Log.i("TAG","Exit"+region.getMajor());
    }


    @Override
    public void onBeaconsDiscovered(Region region, List<Beacon> list) {



        if(list.size()>0){
            Log.i("TAG","Discover 0th"+list.size()+" "+
                    region.getMinor()+" Major ="+list.get(0).getMajor()+" Minor ="+list.get(0).getMinor()
                    +" UUID= "+list.get(0).getProximityUUID().toString());

            if(list.get(0).getMajor()==5 || list.get(0).getMajor()==20974) {
                nearestBeacon = bIDs.get(list.get(0).getMajor());
                tvTemp.setText("Nearest Beacon = " + nearestBeacon);
            }


            if (prev==0) {
                prev = bIDs.get(list.get(0).getMajor());
                Log.d(TAG, "onBeaconsDiscovered: prev set once");
            } else if (bIDs.get(list.get(0).getMajor())!=prev){
                Log.d(TAG, "onBeaconsDiscovered: next set and prev updated");

                if (bIDs.get(list.get(0).getMajor())==1)
                    dialog = "You are near baggage counter. ";
                else
                    dialog = "Find the latest collection of clothes on the Airport Mall on your right!";

                prev=next;
                next=bIDs.get(list.get(0).getMajor());
                checkDirection(next-prev);


            }

        } else {
            next=0;
        }


    }

    private void checkDirection(int diff) {
        if(diff>0) {
            giveUpdate(true); //next dir
        } else if (diff<0) {
            giveUpdate(false); //wrong dir
        }
    }

    private void giveUpdate(final boolean isRight) {
        Log.d(TAG, "giveUpdate: "+isRight);

            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {

                        int result = textToSpeech.setLanguage(Locale.US);

                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "This Language is not supported");
                        } else {
                            speakOut(isRight);
                        }

                    } else {
                        Log.e("TTS", "Initilization Failed!");
                    }
                }
            });
        speakOut(isRight);
    }

    private void speakOut(boolean isRight) {
        if (isRight) {
            textToSpeech.speak("You are on the right path to the gate. Move to your right.", TextToSpeech.QUEUE_ADD, null);
        } else {
            textToSpeech.speak("Moving in wrong direction", TextToSpeech.QUEUE_ADD, null);
            Vibrator vibrate = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
            long[] pattern = {0, 1000, 500, 1000, 800, 1000};
            vibrate.vibrate(pattern, -1);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP&& countVolUp >=3) {

            String pName = "Mr. Ram Kumar";
            SmsManager smsManager = SmsManager.getDefault();

            if(nearestBeacon!=0){
                textToSpeech.speak("Sending assistance. Please wait.",TextToSpeech.QUEUE_FLUSH, null);
                smsManager.sendTextMessage("95821847", null, "Help needed to "+pName+" near beacon number "+nearestBeacon+" ", null, null);
            }

            countVolUp =0;
        } else {
            countVolUp++;
        }

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && countVolDown >=3){

            textToSpeech.speak("Speak after beep sound ",TextToSpeech.QUEUE_FLUSH, null);
            displaySpeechRecognizer();
            countVolDown =0;
        }
        else {
            countVolDown++;
        }

        return true;

    }

    @Override
    protected void onDestroy() {
        if ( textToSpeech!= null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            Log.i("TAG","Text " + spokenText);
            if(spokenText.equals("locate me")||spokenText.equals("where am i")||spokenText.equals("Locate me")||spokenText.equals("where am I")){

                Intent intent = new Intent(MainActivity.this, PopupActivity.class);

                Log.i("TAG","dialog " + dialog);
                if(!dialog.equals("")){

                    intent.putExtra("Notification", dialog);
                    startActivity(intent);
                }

            }



        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View view) {
       if(view==ivHelp){
           String pName = "Mr. Ram Kumar";
           SmsManager smsManager = SmsManager.getDefault();

           if(nearestBeacon!=0){
               textToSpeech.speak("Sending assistance. Please wait.",TextToSpeech.QUEUE_FLUSH, null);
               smsManager.sendTextMessage("95821847", null, "Help needed to "+pName+" near beacon number "+nearestBeacon+" ", null, null);
           }
       }

       else if(view==ivLoc){
           if(!dialog.equals("")){

               Intent intent = new Intent(MainActivity.this, PopupActivity.class);
               intent.putExtra("Notification", dialog);
               startActivity(intent);
           }

        }

        else{
            displaySpeechRecognizer();
       }


    }
}
