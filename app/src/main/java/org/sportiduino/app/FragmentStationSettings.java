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
import android.widget.RadioButton;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FragmentStationSettings extends NfcFragment {
    private FragmentStationSettingsBinding binding;
    private Password password = Password.defaultPassword();
    private ArrayList<RadioButton> listRadioButtons;
    private CardType cardType = CardType.UNKNOWN;

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        String passwordStr = sharedPref.getString("password", Password.defaultPassword().toString());
        this.password = Password.fromString(passwordStr);

        int count = binding.radioGroup.getChildCount();
        listRadioButtons = new ArrayList<>();

        for (int i = 0; i < count; ++i) {
            final View v = binding.radioGroup.getChildAt(i);

            if (v instanceof RadioButton) {
                RadioButton rb = (RadioButton) v;
                listRadioButtons.add(rb);
                rb.setOnClickListener(rbClickListener);
            }
        }
    }

    View.OnClickListener rbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton rb = (RadioButton) v;
            if (listRadioButtons.contains(rb)) {
                binding.textViewInfo.setText(R.string.bring_card);
            }
            binding.layoutStationNumber.setVisibility(View.GONE);
            switch (rb.getId()) {
                case R.id.radio_button_master_get_state:
                    cardType = CardType.MASTER_GET_STATE;
                    break;
                case R.id.radio_button_master_time:
                    cardType = CardType.MASTER_SET_TIME;
                    break;
                case R.id.radio_button_master_number:
                    cardType = CardType.MASTER_SET_NUMBER;
                    binding.layoutStationNumber.setVisibility(View.VISIBLE);
                    break;
                case R.id.radio_button_master_sleep:
                    cardType = CardType.MASTER_SLEEP;
                    break;
                case R.id.radio_button_master_config:
                    cardType = CardType.MASTER_CONFIG;
                    break;
                case R.id.radio_button_master_backup:
                    cardType = CardType.MASTER_READ_BACKUP;
                    break;
                default:
                    cardType = CardType.UNKNOWN;
            }
        }
    };

    @Override
    public void onNewTagDetected(Tag tag) {
        if (cardType == CardType.UNKNOWN) {
            return;
        }
        String[] techList = tag.getTechList();
        CardAdapter adapter = null;
        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                adapter = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                adapter = new CardMifareUltralight(MifareUltralight.get(tag));
            }
            if (adapter != null) {
                MasterCard masterCard = new MasterCard(adapter, cardType, password);
                if (binding.radioButtonMasterNumber.isChecked()) {
                    String str = binding.editTextStationNumber.getText().toString();
                    int stationNumber;
                    try {
                        stationNumber = Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        stationNumber = 0;
                    }
                    if (stationNumber == 0) {
                        binding.textViewInfo.setText("Insert station number.");
                        return;
                    }
                    masterCard.dataForWriting = MasterCard.packStationNumber(stationNumber);
                } else if (binding.radioButtonMasterTime.isChecked()) {
                    masterCard.dataForWriting = MasterCard.packTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                }
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
