package org.sportiduino.app;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

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

        binding.textViewNfcInfo.setText(R.string.bring_card);

        binding.editTextCardNumber.setFilters(new InputFilter[]{new MinMaxFilter(1, 65535)});

        binding.checkBoxCleaningOnly.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                binding.editTextCardNumber.setEnabled(false);
                binding.checkBoxAutoIncrement.setEnabled(false);
            } else {
                binding.editTextCardNumber.setEnabled(true);
                binding.checkBoxAutoIncrement.setEnabled(true);
            }
        });
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
            binding.textViewNfcInfo.setText(R.string.insert_card_number);
            return;
        }

        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                adapter = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                adapter = new CardMifareUltralight(MifareUltralight.get(tag));
            }
            if (adapter != null) {
                if (binding.checkBoxCleaningOnly.isChecked()) {
                    cardNumber = -1;
                }
                ParticipantCard card = new ParticipantCard(adapter, cardNumber, binding.checkBoxFastPunch.isChecked());
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
            binding.textViewNfcInfo.setText(R.string.writing_card_dont_remove_it);
            binding.progressBar.setVisibility(View.VISIBLE);
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
            binding.progressBar.setVisibility(View.GONE);
            if (result) {
                binding.textViewNfcInfo.setText(Util.ok(getString(R.string.data_written_to_card_successfully)));
                if (binding.checkBoxAutoIncrement.isChecked() && !binding.checkBoxCleaningOnly.isChecked()) {
                    binding.editTextCardNumber.setText(String.valueOf(cardNumber + 1));
                }
            } else {
                binding.textViewNfcInfo.setText(Util.error(getString(R.string.writing_card_failed)));
            }
        }
    }
}
