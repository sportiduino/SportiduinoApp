package org.sportiduino.app;

import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.sportiduino.app.databinding.FragmentStationSettingsBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardAdapter;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.CardType;
import org.sportiduino.app.sportiduino.MasterCard;
import org.sportiduino.app.sportiduino.ReadWriteCardException;
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

        binding.textViewInfo.setText(R.string.bring_card);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        String passwordStr = sharedPref.getString("password", Password.defaultPassword().toString());
        this.password = Password.fromString(passwordStr);
    }

    @Override
    public void onNewTagDetected(Tag tag) {
        String[] techList = tag.getTechList();
        CardAdapter adapter = null;
        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                adapter = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                adapter = new CardMifareUltralight(MifareUltralight.get(tag));
            }
            if (adapter != null) {
                MasterCard masterCard = new MasterCard(adapter, CardType.MASTER_GET_STATE, password);
                new WriteCardTask(masterCard).execute();
                break;
            }
        }
    }

    class WriteCardTask extends AsyncTask<Void, Void, Boolean> {
        final private Card card;

        WriteCardTask(Card card) {
            this.card = card;
        }

        @Override
        protected void onPreExecute() {
            binding.textViewInfo.setText("Writing card, don't remove it...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                card.write();
                return true;
            } catch (ReadWriteCardException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                binding.textViewInfo.setText(Util.ok("Data written to card successfully"));
            } else {
                binding.textViewInfo.setText(Util.error("Writing card failed!"));
            }
        }
    }

}
