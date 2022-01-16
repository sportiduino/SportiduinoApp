package org.sportiduino.app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentStationSettings extends NfcFragment {
    private FragmentStationSettingsBinding binding;
    private Password password = Password.defaultPassword();
    private ArrayList<RadioButton> listRadioButtons;
    private CardType cardType = CardType.UNKNOWN;
    Calendar wakeupTime = Calendar.getInstance();
    private int timerCount;
    private Timer timer;
    //private final DateFormat dateFormat = new SimpleDateFormat();

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

        binding.textViewWakeupDate.setPaintFlags(binding.textViewWakeupDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.textViewWakeupDate.setOnClickListener(dateClickListener);

        binding.textViewWakeupTime.setPaintFlags(binding.textViewWakeupTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.textViewWakeupTime.setOnClickListener(timeClickListener);

        updateWakeupTime();
    }

    View.OnClickListener dateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int year = wakeupTime.get(Calendar.YEAR);
            int month = wakeupTime.get(Calendar.MONTH);
            int day = wakeupTime.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(getActivity(), dateSetListener, year, month, day).show();
        }
    };

    View.OnClickListener timeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //DialogFragment newFragment = new TimePickerFragment(this);
            //newFragment.show(getParentFragmentManager(), "timePicker");
            int hour = wakeupTime.get(Calendar.HOUR_OF_DAY);
            int minute = wakeupTime.get(Calendar.MINUTE);

            new TimePickerDialog(getActivity(), timeSetListener, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity())).show();
        }
    };

    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            wakeupTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            wakeupTime.set(Calendar.MINUTE, minute);
            wakeupTime.set(Calendar.SECOND, 0);
            updateWakeupTime();
        }
    };

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            wakeupTime.set(Calendar.YEAR, year);
            wakeupTime.set(Calendar.MONTH, month);
            wakeupTime.set(Calendar.DAY_OF_MONTH, day);
            updateWakeupTime();
        }
    };

    private void updateWakeupTime() {
        binding.textViewWakeupDate.setText(DateFormat.getDateInstance().format(wakeupTime.getTime()));
        binding.textViewWakeupTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(wakeupTime.getTime()));
    }

    View.OnClickListener rbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton rb = (RadioButton) v;
            if (listRadioButtons.contains(rb)) {
                binding.textViewInfo.setText(R.string.bring_card);
            }
            binding.layoutStationNumber.setVisibility(View.GONE);
            binding.layoutWakeupTime.setVisibility(View.GONE);
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
                    binding.layoutWakeupTime.setVisibility(View.VISIBLE);
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
                    Calendar c = Calendar.getInstance();
                    Log.i("current", String.valueOf(c.get(Calendar.SECOND)));
                    c.add(Calendar.SECOND, 3);
                    Log.i("tocard", String.valueOf(c.get(Calendar.SECOND)));
                    masterCard.dataForWriting = MasterCard.packTime(c);
                } else if (binding.radioButtonMasterSleep.isChecked()) {
                    masterCard.dataForWriting = MasterCard.packTime(wakeupTime);
                }
                new WriteCardTask(masterCard).execute();
                break;
            }
        }
    }

    private void startCountdownTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        CountdownTimerTask countdownTimerTask = new CountdownTimerTask();
        timerCount = 3;
        timer.scheduleAtFixedRate(countdownTimerTask, 0, 1000);
    }

    class CountdownTimerTask extends TimerTask {

        @Override
        public void run() {
            requireActivity().runOnUiThread(() -> {
                String timerStr = String.valueOf(timerCount);
                if (timerCount == 0) {
                    timerStr = "Beep";
                } else if (timerCount < 0) {
                    timerStr = "";
                    timer.cancel();
                    timer = null;
                }
                binding.textViewTimer.setText(timerStr);
                --timerCount;
            });
        }
	}

    //public static class TimePickerFragment extends DialogFragment
    //        implements TimePickerDialog.OnTimeSetListener {

    //    private FragmentStationSettings fragmentStationSettings;

    //    public TimePickerFragment(FragmentStationSettings f) {
    //        super();
    //        fragmentStationSettings = f;
    //    }

    //    @Override
    //    public Dialog onCreateDialog(Bundle savedInstanceState) {
    //        int hour = wakeupTime.get(Calendar.HOUR_OF_DAY);
    //        int minute = wakeupTime.get(Calendar.MINUTE);

    //        return new TimePickerDialog(getActivity(), this, hour, minute,
    //                android.text.format.DateFormat.is24HourFormat(getActivity()));
    //    }

    //    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    //        // Do something with the time chosen by the user
    //        Log.i("onClick", "time");
    //    }
    //}

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
                if (binding.radioButtonMasterTime.isChecked()) {
                    startCountdownTimer();
                }
            } else {
                binding.textViewInfo.setText(Util.error("Writing card failed!"));
            }
        }
    }
}

