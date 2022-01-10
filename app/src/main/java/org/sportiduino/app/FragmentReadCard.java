package org.sportiduino.app;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.sportiduino.app.databinding.FragmentReadCardBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.Util;

public class FragmentReadCard extends NfcFragment implements IntentReceiver {
    private FragmentReadCardBinding binding;

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
    }

    @Override
    public void onNewTagDetected(Tag tag) {
        String tagInfo = "Tag ID: ";
        byte[] tagId = tag.getId();
        for (byte b : tagId) {
            tagInfo += Integer.toHexString(b & 0xFF) + " ";
        }
        binding.textViewInfo.setText(tagInfo);

        String[] techList = tag.getTechList();
        Card card = null;
        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                card = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                card = new CardMifareUltralight(MifareUltralight.get(tag));
            }
            if (card != null) {
                new ReadCardTask(card, setText, setTagType).execute();
                break;
            }
        }
    }

    public Util.Callback setText = (str) -> binding.textViewTagInfo.setText(str);
    public Util.Callback setTagType = (str) -> binding.textViewInfo.setText(binding.textViewInfo.getText() + " " + str);
}
