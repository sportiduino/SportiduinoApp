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

import org.sportiduino.app.databinding.FragmentReadCardBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardAdapter;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.ReadWriteCardException;
import org.sportiduino.app.sportiduino.Util;

public class FragmentReadCard extends NfcFragment implements IntentReceiver {
    private FragmentReadCardBinding binding;
    private View currentView;

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
        this.currentView = view;

        binding.textViewNfcInfo.setText(R.string.bring_card);
    }

    @Override
    public void onNewTagDetected(Tag tag) {
        StringBuilder tagInfo = new StringBuilder();
        byte[] tagId = tag.getId();
        for (byte b : tagId) {
            tagInfo.append(Integer.toHexString(b & 0xFF)).append(" ");
        }
        String tagIdStr = tagInfo.toString();
        binding.textViewTagId.setText(String.format(getString(R.string.tag_id_s), tagIdStr));
        binding.layoutTagInfo.setVisibility(View.VISIBLE);

        String[] techList = tag.getTechList();
        CardAdapter adapter = null;
        for (String s : techList) {
            if (s.equals(MifareClassic.class.getName())) {
                adapter = new CardMifareClassic(MifareClassic.get(tag));
            } else if (s.equals(MifareUltralight.class.getName())) {
                adapter = new CardMifareUltralight(MifareUltralight.get(tag));
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

        ReadCardTask(CardAdapter adapter) {
            this.cardAdapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            binding.textViewNfcInfo.setText(R.string.reading_card_dont_remove_it);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = true;
            try {
                cardAdapter.connect();
                card = Card.detectCard(cardAdapter);
                buffer = card.read();
            } catch (ReadWriteCardException e) {
                status = false;
            } finally {
                cardAdapter.close();
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && buffer != null) {
                binding.textViewTagType.setText(String.format(getString(R.string.tag_type_s), card.adapter.tagType.name()));
                binding.textViewInfo.setVisibility(View.VISIBLE);
                binding.textViewInfo.setText(card.parseData(buffer));
                binding.textViewNfcInfo.setText(Util.ok(getString(R.string.card_read_successfully), currentView));
            } else {
                binding.textViewTagType.setText("");
                binding.textViewInfo.setVisibility(View.GONE);
                binding.textViewNfcInfo.setText(Util.error(getString(R.string.reading_card_failed), currentView));
            }
        }
    }
}
