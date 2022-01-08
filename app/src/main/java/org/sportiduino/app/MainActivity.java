package org.sportiduino.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    TextView textViewInfo, textViewTagInfo;
    PendingIntent pendingIntent;
    String[][] techList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textViewInfo = (TextView) findViewById(R.id.info);
        textViewTagInfo = (TextView) findViewById(R.id.taginfo);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        textViewInfo.setText("Bring card...");
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        techList = new String[][] {new String[] {MifareClassic.class.getName()}};
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String tagInfo = "Tag ID: ";
            byte[] tagId = tag.getId();
            for (byte b : tagId) {
                tagInfo += Integer.toHexString(b & 0xFF) + " ";
            }
            textViewInfo.setText(tagInfo);

            String[] techList = tag.getTechList();
            for (String s : techList) {
                if (s.equals(MifareClassic.class.getName())) {
                    CardMifareClassic cardMifareClassic = new CardMifareClassic(MifareClassic.get(tag));
                    new ReadCardTask((Card) cardMifareClassic, setText).execute();
                } else if (s.equals(MifareUltralight.class.getName())) {
                    CardMifareUltralight cardMifareUltralight = new CardMifareUltralight(MifareUltralight.get(tag));
                    new ReadCardTask(cardMifareUltralight, setText).execute();
                }
            }
        }
    }

    public ReadCardTask.Callback setText = (str) -> { textViewTagInfo.setText(str); };
}
