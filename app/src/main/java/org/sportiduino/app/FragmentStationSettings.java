package org.sportiduino.app;

import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.sportiduino.app.databinding.FragmentStationSettingsBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.CardType;
import org.sportiduino.app.sportiduino.Util;

public class FragmentStationSettings extends NfcFragment {
    private FragmentStationSettingsBinding binding;
    private Password password = Password.defaultPassword();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentStationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textViewInfo1.setText(R.string.bring_card);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        String passwordStr = sharedPref.getString("password", Password.defaultPassword().toString());
        this.password = Password.fromString(passwordStr);
    }

    @Override
    public void onNewTagDetected(Tag tag) {
        String[] techList = tag.getTechList();
        Card card = null;
        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                card = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                card = new CardMifareUltralight(MifareUltralight.get(tag));
            }
            if (card != null) {
                card.type = CardType.MASTER_GET_STATE;
                new WriteCardTask(card, setText, password).execute();
                break;
            }
        }
    }

    public Util.Callback setText = (str) -> binding.textViewInfo1.setText(str);
}
