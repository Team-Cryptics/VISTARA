package samarthgupta.com.vistara;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class PopupActivity extends AppCompatActivity {

    TextToSpeech tts;
    String notific;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        notific = getIntent().getStringExtra("notific");
        ((TextView) findViewById(R.id.tvNotific)).setText(notific);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i==TextToSpeech.SUCCESS) {
                    speak(notific);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 5000);
    }

    private void speak(String notific) {
        tts.speak(notific, TextToSpeech.QUEUE_ADD, null);
    }
}
