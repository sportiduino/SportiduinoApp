package org.sportiduino.app;

import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;
import org.sportiduino.app.databinding.FragmentReadCardBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardAdapter;
import org.sportiduino.app.sportiduino.CardData;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.ParticipantCard;
import org.sportiduino.app.sportiduino.ReadWriteCardException;
import org.sportiduino.app.sportiduino.Util;

import java.io.IOException;
import java.util.Objects;
import java.util.LinkedList;
import java.util.List;

public class FragmentReadCard extends NfcFragment {
    private FragmentReadCardBinding binding;
    private View currentView;
    private String cardDataUrl;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentReadCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentView = view;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        this.cardDataUrl = sharedPref.getString("card_data_url", "null");

        binding.textViewNfcInfo.setText(R.string.bring_card);
    }

    @Override
    public void onNewTagDetected(Tag tag) {
        List<String> strings = new LinkedList<>();
        byte[] tagId = tag.getId();
        for (byte b : tagId) {
            strings.add(String.format("%02x", b));
        }
        String tagIdStr = String.join(":", strings);
        binding.textViewTagId.setText(String.format(getString(R.string.tag_id_s), tagIdStr));
        binding.layoutTagInfo.setVisibility(View.VISIBLE);

        String[] techList = tag.getTechList();
        CardAdapter adapter = null;
        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                adapter = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                NtagAuthKey authKey = NtagAuthKeyManager.getAuthKey(requireActivity());
                adapter = new CardMifareUltralight(MifareUltralight.get(tag), authKey);
            }
            if (adapter != null) {
                new ReadCardTask(adapter).execute();
                break;
            }
        }
    }

    class ReadCardTask extends AsyncTask<Void, Void, Boolean> {
        CardAdapter cardAdapter;
        Card card;
        byte[][] buffer;
        CardData parsedData;
        Exception error;
        IOException csvSaveError;

        ReadCardTask(CardAdapter adapter) {
            this.cardAdapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            binding.textViewTagType.setText("");
            binding.textViewInfo.setVisibility(View.GONE);
            binding.textViewInfo.setText("");
            binding.textViewNfcInfo.setText(R.string.reading_card_dont_remove_it);
            binding.textViewCardWebServiceInfo.setVisibility(View.GONE);
            binding.textViewCardWebServiceInfo.setText("");
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                cardAdapter.connect();

                card = Card.detectCard(cardAdapter);
                buffer = card.read();

                parsedData = card.parseData(buffer);

                if (parsedData instanceof ParticipantCard.Data) {
                    try {
                        ParticipantCard.Data participantData =
                                (ParticipantCard.Data) parsedData;

                        ParticipantCardCsvWriter csvWriter =
                                new ParticipantCardCsvWriter(getContext());

                        csvWriter.save(participantData);
                    } catch (IOException e) {
                        csvSaveError = e;
                        Log.e("Sportiduino", "CSV save error", e);
                    }
                }

                return true;

            } catch (ReadWriteCardException e) {
                error = e;
                Log.e("Sportiduino", "Card read error", e);
                return false;

            } finally {
                cardAdapter.close();
            }
        }

        private void handleCardDataUrl(CharSequence data) {
            CardWebService service = new CardWebService(getContext(), cardDataUrl, new CardWebServiceCallback() {
                @Override
                public void onOk(JSONObject json) {
                    binding.textViewCardWebServiceInfo.setVisibility(View.VISIBLE);
                    binding.textViewCardWebServiceInfo.setText(json.toString());
                }

                @Override
                public void onError(Exception e) {
                    binding.textViewCardWebServiceInfo.setVisibility(View.VISIBLE);
                    binding.textViewCardWebServiceInfo.setText(e.getMessage());
                }
            });

            service.send(data);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            binding.progressBar.setVisibility(View.GONE);

            if (result && buffer != null) {
                CharSequence text = parsedData.toCharSequence();

                binding.textViewTagType.setText(String.format(getString(R.string.tag_type_s), card.adapter.tagType.name()));
                binding.textViewInfo.setVisibility(View.VISIBLE);
                binding.textViewInfo.setText(text);
                binding.textViewNfcInfo.setText(Util.ok(getString(R.string.card_read_successfully), currentView));

                handleCardDataUrl(text); // FIXME: send data

                if (csvSaveError != null) {
                    binding.textViewCardWebServiceInfo.setVisibility(View.VISIBLE);
                    binding.textViewCardWebServiceInfo.setText(
                            "CSV save error: " + csvSaveError.getMessage()
                    );
                }

            } else {
                binding.textViewNfcInfo.setText(Util.error(getString(R.string.reading_card_failed), currentView));
            }
        }
    }
}
