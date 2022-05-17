package org.sportiduino.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public abstract class NfcFragment extends Fragment implements IntentReceiver {
    private NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    String[][] techList;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, Objects.requireNonNull(activity).getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE);
        techList = new String[][]{
                new String[]{MifareClassic.class.getName()},
                new String[]{MifareUltralight.class.getName()}
        };
        setThisAsIntentReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        setThisAsIntentReceiver();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, null, techList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(getActivity());
        }
    }

    private void setThisAsIntentReceiver() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setIntentReceiver(this);
        }
    }

    public abstract void onNewTagDetected(Tag tag);

    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            onNewTagDetected(tag);
        }
    }
}
