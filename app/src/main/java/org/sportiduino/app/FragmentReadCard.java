package org.sportiduino.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.sportiduino.app.databinding.FragmentReadCardBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;

public class FragmentReadCard extends Fragment implements IntentReceiver {
    private FragmentReadCardBinding binding;
    private NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    String[][] techList;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentReadCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textViewInfo.setText("Bring card...");
        Activity activity = getActivity();
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        techList = new String[][]{new String[]{MifareClassic.class.getName()}};
        ((MainActivity) activity).setIntentReceiver(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(getActivity());
        }
    }

    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String tagInfo = "Tag ID: ";
            byte[] tagId = tag.getId();
            for (byte b : tagId) {
                tagInfo += Integer.toHexString(b & 0xFF) + " ";
            }
            binding.textViewInfo.setText(tagInfo);

            String[] techList = tag.getTechList();
            for (String s : techList) {
                if (s.equals(MifareClassic.class.getName())) {
                    CardMifareClassic cardMifareClassic = new CardMifareClassic(MifareClassic.get(tag));
                    new ReadCardTask((Card) cardMifareClassic, setText, setTagType).execute();
                } else if (s.equals(MifareUltralight.class.getName())) {
                    CardMifareUltralight cardMifareUltralight = new CardMifareUltralight(MifareUltralight.get(tag));
                    new ReadCardTask(cardMifareUltralight, setText, setTagType).execute();
                }
            }
        }
    }

    public ReadCardTask.Callback setText = (str) -> { binding.textViewTagInfo.setText(str); };
    public ReadCardTask.Callback setTagType = (str) -> {
        binding.textViewInfo.setText(binding.textViewInfo.getText() + " " + str);
    };
}
