package org.sportiduino.app;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.sportiduino.app.databinding.FragmentWriteCardBinding;
import org.sportiduino.app.sportiduino.CardAdapter;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.ParticipantCard;
import org.sportiduino.app.sportiduino.ReadWriteCardException;
import org.sportiduino.app.sportiduino.Util;

public class FragmentWriteCard extends NfcFragment {
    private FragmentWriteCardBinding binding;
    private int cardNumber;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentWriteCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textViewInfo.setText(R.string.bring_card);
    }

    @Override
    public void onNewTagDetected(Tag tag) {
        String[] techList = tag.getTechList();
        CardAdapter adapter = null;

        String cardStr = binding.editTextCardNumber.getText().toString();
        try {
            cardNumber = Integer.parseInt(cardStr);
        } catch (NumberFormatException e) {
            cardNumber = 0;
        }
        if (cardNumber == 0) {
            binding.textViewInfo.setText("Insert card number.");
            return;
        }

        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                adapter = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                adapter = new CardMifareUltralight(MifareUltralight.get(tag));
            }
            if (adapter != null) {
                ParticipantCard card = new ParticipantCard(adapter, cardNumber);
                new WriteCardTask(card).execute();
                break;
            }
        }
    }

    class WriteCardTask extends AsyncTask<Void, Void, Boolean> {
        final private ParticipantCard card;

        WriteCardTask(ParticipantCard card) {
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
                if (binding.checkBoxAutoIncrement.isChecked()) {
                    binding.editTextCardNumber.setText(String.valueOf(cardNumber + 1));
                }
            } else {
                binding.textViewInfo.setText(Util.error("Writing card failed!"));
            }
        }
    }
}
