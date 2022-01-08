package org.sportiduino.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.TagTechnology;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.sportiduino.app.sportiduino.State;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    TextView textViewInfo, textViewTagInfo;
    PendingIntent pendingIntent;
    String[][] techList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textViewInfo = (TextView) findViewById(R.id.info);
        textViewTagInfo = (TextView) findViewById(R.id.taginfo);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        techList = new String[][] {new String[] {MifareClassic.class.getName()}};
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        Toast.makeText(this,
                    "onResume()",
                    Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String tagInfo = "Tag Id: ";
            byte[] tagId = tag.getId();
            for (byte b : tagId) {
                tagInfo += Integer.toHexString(b & 0xFF) + " ";
            }
            textViewInfo.setText(tagInfo);

            String[] techList = tag.getTechList();
            for (String s : techList) {
                if (s.equals(MifareClassic.class.getName())) {
                    CardMifareClassic cardMifareClassic = new CardMifareClassic(MifareClassic.get(tag));
                    new ReadMifareClassicTask((Card) cardMifareClassic).execute();
                } else if (s.equals(MifareUltralight.class.getName())) {
                    CardMifareUltralight cardMifareUltralight = new CardMifareUltralight(MifareUltralight.get(tag));
                    new ReadMifareClassicTask(cardMifareUltralight).execute();
                }
            }
        }
    }

    enum CardType {
        UNKNOWN            (),
        ORDINARY           (),
        MASTER_GET_STATE   ((byte) 0xF9),
        MASTER_SET_TIME    ((byte) 0xFA),
        MASTER_SET_NUMBER  ((byte) 0xFB),
        MASTER_SLEEP       ((byte) 0xFC),
        MASTER_READ_BACKUP ((byte) 0xFD),
        MASTER_SET_PASS    ((byte) 0xFE);

        private static final Map<Byte, CardType> BY_VALUE = new HashMap<>();

        static {
            for (CardType ct : values()) {
                BY_VALUE.put(ct.value, ct);
            }
        }

        final byte value;

        private CardType(byte value) {
            this.value = value;
        }

        private CardType() {
            this((byte) 0);
        }

        public static CardType byValue(byte value) {
            return BY_VALUE.get(value);
        }
    }

    static enum TagType {
        UNKNOWN,
        MIFARE_MINI,
        MIFARE_1K,
        MIFARE_4K,
        MIFARE_UL,
        MIFARE_PLUS,
        NTAG213,
        NTAG215,
        NTAG216
    }

    public abstract class Card {
        protected TagTechnology tagTech;
        public TagType tagType = TagType.UNKNOWN;
        public CardType type = CardType.UNKNOWN;

        public Card(TagTechnology tagTech) {
            this.tagTech = tagTech;
        }

        protected void connect() {
            try {
                if (!tagTech.isConnected()) {
                    tagTech.connect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void close() {
            if(tagTech != null && tagTech.isConnected()){
                try {
                    tagTech.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        abstract byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull);

        public int getMaxPage() {
            switch(tagType) {
                case MIFARE_MINI:
                    return 17;
                case MIFARE_1K:
                    return 50;
                case MIFARE_4K:
                    return 98;
                case MIFARE_UL:
                case NTAG213:
                    return 39;
                case NTAG215:
                    return 129;
                case NTAG216:
                    return 225;
                default:
                    return 0;
            }
        }
    }

    class CardMifareClassic extends Card {
        private MifareClassic tag;
        final int numOfBlockInSector = 4;

        public CardMifareClassic(MifareClassic tag) {
            super((TagTechnology) tag);
            this.tag = tag;
            int size = tag.getSize();
            switch(size){
                case MifareClassic.SIZE_1K:
                    tagType = TagType.MIFARE_1K;
                    break;
                case MifareClassic.SIZE_2K:
                    // FIXME
                    tagType = TagType.MIFARE_1K;
                    break;
                case MifareClassic.SIZE_4K:
                    tagType = TagType.MIFARE_4K;
                    break;
                case MifareClassic.SIZE_MINI:
                    tagType = TagType.MIFARE_MINI;
                    break;
                default:
                    tagType = TagType.UNKNOWN;
            }
        }

        public byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull) {
            byte[][] blockData = new byte[count][MifareClassic.BLOCK_SIZE];
            int lastSector = -1;
            int i = 0;
            int firstBlockIndex = firstPageIndex-3 + ((firstPageIndex-3)/3);
            for (int blockIndex = firstBlockIndex; i < count; ++blockIndex) {
                if ((blockIndex + 1)%numOfBlockInSector == 0) {
                    continue;
                }
                int sector = tag.blockToSector(blockIndex);
                if (sector != lastSector) {
                    lastSector = sector;
                    try {
                        if (!tag.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                            return new byte[0][0];
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    blockData[i++] = tag.readBlock(blockIndex);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (stopIfPageNull && blockData[i][0] == 0) {
                    break;
                }
            }
            return blockData;
        }
    }

    class CardMifareUltralight extends Card {
        private MifareUltralight tag = (MifareUltralight) tagTech;

        public CardMifareUltralight(MifareUltralight tag) {
            super((TagTechnology) tag);
            this.tag = tag;
        }

        @Override
        protected void connect() {
            super.connect();

            byte[] pageData = readPages(3, 1, false)[0];
            
            switch(pageData[2] & 0xFF){
                case 0x12:
                    tagType = TagType.NTAG213;
                    break;
                case 0x3e:
                    tagType = TagType.NTAG215;
                    break;
                case 0x6d:
                    tagType = TagType.NTAG216;
                    break;
                default:
                    tagType = TagType.MIFARE_UL;
            }
        }

        public byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull) {
            byte[][] blockData = new byte[count][16];
            int pageIndex = firstPageIndex;
            for (int i = 0; i < count; ++i) {
                try {
                    blockData[i] = tag.readPages(pageIndex++);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Log.d("Log", i + ": "
                //+ Integer.toHexString(blockData[i][0] & 0xFF) + " "
                //+ Integer.toHexString(blockData[i][1] & 0xFF) + " "
                //+ Integer.toHexString(blockData[i][2] & 0xFF) + " "
                //+ Integer.toHexString(blockData[i][3] & 0xFF));
                if (stopIfPageNull && blockData[i][0] == 0) {
                    break;
                }
            }
            return blockData;
        }
    }

    private class ReadMifareClassicTask extends AsyncTask<Void, Void, Void> {
        static final int CARD_PAGE_INIT = 4;
        static final int CARD_PAGE_INIT_TIME = 5;
        static final int CARD_PAGE_LAST_RECORD_INFO = 6;
        static final int CARD_PAGE_INFO1 = 6;
        static final int CARD_PAGE_INFO2 = 7;
        static final int CARD_PAGE_START = 8;
        static final int CARD_PAGE_PASS = 5;
        static final int CARD_PAGE_DATE = 6;
        static final int CARD_PAGE_TIME = 7;
        static final int CARD_PAGE_STATION_NUM = 6;
        static final int CARD_PAGE_BACKUP_START = 6;
        static final byte MASTER_CARD_SIGN = (byte) 0xFF;

        Card card;
        final int numOfSector = 16;
        final int numOfBlockInSector = 4;
        byte[][] buffer = new byte[numOfSector*numOfBlockInSector][MifareClassic.BLOCK_SIZE];

        ReadMifareClassicTask(Card card){
            this.card = card;
        }

        @Override
        protected void onPreExecute() {
            textViewTagInfo.setText("Reading Tag, don't remove it!");
        }

        @Override
        protected Void doInBackground(Void... params) {

            card.connect();

            byte[] data = card.readPages(CARD_PAGE_INIT, 1, false)[0];

            if (data[2] == MASTER_CARD_SIGN) {
                card.type = CardType.byValue(data[1]);
                if (data[1] == CardType.MASTER_GET_STATE.value) {
                    buffer = card.readPages(8, 12, false);
                } else {
                    card.type = CardType.UNKNOWN;
                }
            } else {
                card.type = CardType.ORDINARY;
                int cardNumber = data[0] & 0xFF;
                cardNumber <<= 8;
                cardNumber |= data[1] & 0xFF;
                buffer = card.readPages(CARD_PAGE_START, card.getMaxPage(), true);
            }

            card.close();
            
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            textViewInfo.setText(textViewInfo.getText().toString() + "\n" + card.tagType.name());
            switch(card.type) {
                case UNKNOWN:
                    break;
                case ORDINARY:
                    String str = "Participant card";
                    for(int i = 0; i < card.getMaxPage(); ++i) {
                        int cp = buffer[i][0] & 0xFF;
                        if (cp == 0) {
                            break;
                        }
                        str += "\n" + cp;
                    }
                    textViewTagInfo.setText(str);
                    break;
                case MASTER_SET_TIME:
                    textViewTagInfo.setText("Time Master card");
                    break;
                case MASTER_SET_NUMBER:
                    textViewTagInfo.setText("Number Master card");
                    break;
                case MASTER_GET_STATE:
                    State state = new State(buffer);
                    textViewTagInfo.setText(state.toString());
                    break;
                case MASTER_SLEEP:
                    textViewTagInfo.setText("Sleep Master card");
                    break;
                case MASTER_READ_BACKUP:
                    textViewTagInfo.setText("Backup Master card");
                    break;
                case MASTER_SET_PASS:
                    break;
                default:
                    textViewTagInfo.setText("Fail to read card!!!");
                    break;
            }
        }
    }
}
