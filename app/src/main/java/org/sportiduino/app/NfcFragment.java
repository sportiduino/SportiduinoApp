package org.sportiduino.app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.sportiduino.app.sportiduino.Util;

import java.util.Objects;

public abstract class NfcFragment extends Fragment implements IntentReceiver {
    private NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    String[][] techList;

    private View currentView;
    private TextView textViewNfc;
    private TextView textViewNfcInfo;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                updateTextViewNfc();
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentView = view;

        textViewNfc = view.findViewById(R.id.text_view_nfc);
        textViewNfcInfo = view.findViewById(R.id.text_view_nfc_info);

        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        Objects.requireNonNull(getContext()).registerReceiver(receiver, filter);

        updateTextViewNfc();

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
    public void onDestroyView() {
        super.onDestroyView();

        Objects.requireNonNull(getContext()).unregisterReceiver(receiver);
    }

    protected void updateTextViewNfc() {
        if (textViewNfc == null || textViewNfcInfo == null) {
            return;
        }

        boolean isNfcEnabled = Util.isNfcEnabled(currentView);

        if (isNfcEnabled) {
            textViewNfc.setVisibility(GONE);
            textViewNfcInfo.setVisibility(VISIBLE);
        } else {
            textViewNfc.setVisibility(VISIBLE);
            textViewNfcInfo.setVisibility(GONE);
        }
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
