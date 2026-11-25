package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.*;

import android.text.Html;

import org.sportiduino.app.App;
import org.sportiduino.app.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParticipantCard extends Card {

    public static class Data implements CardData {
        public int cardNumber;
        public boolean fastPunch;
        public long clearTimestamp;
        public List<Punch> punches = new ArrayList<>();

        public static class Punch {
            public int controlPoint;
            public long timestamp;
        }

        @Override
        public CharSequence toCharSequence() {
            StringBuilder str = new StringBuilder();

            str.append(App.str(R.string.participant_card_no_))
                    .append(" <b>").append(cardNumber).append("</b>");

            if (fastPunch) {
                str.append(String.format(" (%s)", App.str(R.string.fast_punch)));
            }

            str.append("<br>")
                    .append(App.str(R.string.clear_time_))
                    .append(" ")
                    .append(Util.dformat.format(new Date(clearTimestamp*1000)));

            str.append("<br>")
                    .append(App.str(R.string.record_count))
                    .append(" ")
                    .append(punches.size());

            for (Data.Punch punch : punches) {
                int cp = punch.controlPoint;

                String cpStr = String.valueOf(cp);

                switch (cp) {
                    case Config.START_STATION:
                        cpStr = App.str(R.string.start);
                        break;

                    case Config.FINISH_STATION:
                        cpStr = App.str(R.string.finish);
                        break;
                }

                String timestampString = Util.dformat.format(
                        new Date(punch.timestamp * 1000));

                if (punch.timestamp > System.currentTimeMillis() / 1000) {
                    timestampString = Util.coloredHtmlString(
                            timestampString,
                            Util.colorToHexCode(R.color.red));
                }

                str.append("<br>")
                        .append(String.format("%1$6s", cpStr).replace(" ", "&nbsp;"))
                        .append(" - ")
                        .append(timestampString);
            }

            return Html.fromHtml(str.toString(), Html.FROM_HTML_MODE_LEGACY);
        }
    }

    int cardNumber;
    long cardInitTimestamp;
    boolean fastPunch;
    boolean writeProtection = false;
    boolean readProtection = false;

    public ParticipantCard(CardAdapter adapter, int cardNumber, boolean fastPunch, long cardInitTimestamp) {
        this(adapter, cardNumber, fastPunch);
        this.cardInitTimestamp = cardInitTimestamp;
    }

    public ParticipantCard(CardAdapter adapter, int cardNumber, boolean fastPunch) {
        super(adapter);

        this.type = CardType.ORDINARY;
        this.cardNumber = cardNumber;
        this.fastPunch = fastPunch;
    }

    public void setWriteReadProtection(boolean writeProtection, boolean readProtection) {
        this.writeProtection = writeProtection;
        this.readProtection = readProtection;
    }

    @Override
    public byte[][] read() throws ReadWriteCardException {
        return adapter.readPages(CARD_PAGE_START, adapter.getMaxPage(), true);
    }

    @Override
    public Data parseData(byte[][] data) {
        Data result = new Data();

        result.cardNumber = cardNumber;
        result.fastPunch = fastPunch;
        result.clearTimestamp = cardInitTimestamp;

        long timeHighPart = cardInitTimestamp & 0xFF000000L;

        for (byte[] datum : data) {
            int cp = datum[0] & 0xFF;

            if (cp == 0) {
                break;
            }

            long timestamp = (Util.toUint32(datum) & 0xFFFFFFL)
                    + timeHighPart;

            if (timestamp < cardInitTimestamp) {
                timestamp += 0x1000000L;
            }

            Data.Punch punch = new Data.Punch();
            punch.controlPoint = cp;
            punch.timestamp = timestamp;

            result.punches.add(punch);
        }

        return result;
    }

    @Override
    protected void writeImpl() throws ReadWriteCardException {
        adapter.clear(CARD_PAGE_INFO1, adapter.getMaxPage());
        if (fastPunch) {
            adapter.writePage(CARD_PAGE_INFO1, new byte[]{0, 0, 0, FAST_PUNCH_SIGN});
        }
        long currentTimestamp = System.currentTimeMillis() / 1000;
        adapter.writePage(CARD_PAGE_INIT_TIME, Util.fromUint32(currentTimestamp));
        if (cardNumber > 0) {  // else cleaning only
            final byte[] cardNumberArray = Util.fromUint16(cardNumber);
            final byte[] dataPageInit = {cardNumberArray[0], cardNumberArray[1], 0, FW_PROTO_VERSION};
            adapter.writePage(CARD_PAGE_INIT, dataPageInit);
        }
        adapter.enableDisableAuthentication(writeProtection, readProtection);
    }
}
