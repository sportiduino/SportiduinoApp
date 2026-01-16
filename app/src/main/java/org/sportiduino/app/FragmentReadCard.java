package org.sportiduino.app;

import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;
import org.sportiduino.app.databinding.FragmentReadCardBinding;
import org.sportiduino.app.sportiduino.Card;
import org.sportiduino.app.sportiduino.CardAdapter;
import org.sportiduino.app.sportiduino.CardMifareClassic;
import org.sportiduino.app.sportiduino.CardMifareUltralight;
import org.sportiduino.app.sportiduino.CardType;
import org.sportiduino.app.sportiduino.ReadWriteCardException;
import org.sportiduino.app.sportiduino.Util;

import java.util.ArrayList;
import java.util.Objects;
import java.util.LinkedList;
import java.util.List;

public class FragmentReadCard extends NfcFragment {
    private FragmentReadCardBinding binding;
    private View currentView;
    private String cardDataUrl;
    private CourseValidator courseValidator;

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

        String cardDataCourseValidation = sharedPref.getString("card_course_validation", "null");
        this.courseValidator = new CourseValidator(cardDataCourseValidation);

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

        ReadCardTask(CardAdapter adapter) {
            this.cardAdapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            binding.textViewTagType.setText("");
            binding.textViewCourseValidationInfo.setVisibility(View.GONE);
            binding.textViewInfo.setVisibility(View.GONE);
            binding.textViewInfo.setText("");
            binding.textViewNfcInfo.setText(R.string.reading_card_dont_remove_it);
            binding.textViewCardWebServiceInfo.setVisibility(View.GONE);
            binding.textViewCardWebServiceInfo.setText("");
            binding.progressBar.setVisibility(View.VISIBLE);
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

        private void handleCourseValidation(CharSequence data) {
            if (courseValidator.getType() == Course.CourseType.UNKNOWN) {
                binding.textViewCourseValidationInfo.setVisibility(View.GONE);

                return;
            }

            courseValidator.parse(data.toString());

            Boolean isCourseValid = courseValidator.isValid();

            String validationText = App.str(isCourseValid ? R.string.course_validation_success : R.string.course_validation_failure);
            Integer validationColor = isCourseValid ? R.color.green : R.color.red;

            String validationInfo = App.str(R.string.course_validation) +
                    ": " + Util.coloredHtmlString(validationText, Util.colorToHexCode(validationColor));

            validationInfo += " (";

            validationInfo += courseValidator.getScore() + " points scored";

            if (!isCourseValid) {
                ArrayList<Integer> missedCoursePoints = courseValidator.getMissedCoursePoints();

                if (!missedCoursePoints.isEmpty()) {
                    validationInfo += ", missed point" + (missedCoursePoints.size() > 1 ? "s" : "") +
                            " " + TextUtils.join(", ", missedCoursePoints);
                }
            }

            validationInfo += ")";

            binding.textViewCourseValidationInfo.setText(Html.fromHtml(validationInfo));
            binding.textViewCourseValidationInfo.setVisibility(View.VISIBLE);
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
                CharSequence data = card.parseData(buffer);

                binding.textViewTagType.setText(String.format(getString(R.string.tag_type_s), card.adapter.tagType.name()));
                binding.textViewInfo.setVisibility(View.VISIBLE);
                binding.textViewInfo.setText(data);
                binding.textViewNfcInfo.setText(Util.ok(getString(R.string.card_read_successfully), currentView));

                if (card.type == CardType.ORDINARY) {
                    handleCardDataUrl(data);
                    handleCourseValidation(data);
                }
            } else {
                binding.textViewNfcInfo.setText(Util.error(getString(R.string.reading_card_failed), currentView));
            }
        }
    }
}
