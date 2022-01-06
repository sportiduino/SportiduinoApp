package org.sportiduino.app;

import static org.sportiduino.app.MainActivity.CardType.MASTER_GET_STATE;
import static org.sportiduino.app.MainActivity.CardType.UNKNOWN;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

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
                    readMifareClassic(tag);
                } else if (s.equals(MifareUltralight.class.getName())) {
                    MifareUltralight mfuTag = MifareUltralight.get(tag);
                    String typeInfoString = "--- MifareUltralight tag ---\n";
                    int type = mfuTag.getType();
                    switch(type) {
                        case MifareUltralight.TYPE_ULTRALIGHT:
                            typeInfoString += "MifareUltralight.TYPE_ULTRALIGHT\n";
                            break;
                        case MifareUltralight.TYPE_ULTRALIGHT_C:
                            typeInfoString += "MifareUltralight.TYPE_ULTRALIGHT_C\n";
                            break;
                    }
                    textViewTagInfo.setText(typeInfoString);
                }
            }
        }
    }

    public void readMifareClassic(Tag tag){
        MifareClassic mifareClassicTag = MifareClassic.get(tag);

        String typeInfoString = "--- MifareClassic tag ---\n";
        int type = mifareClassicTag.getType();
        switch(type){
            case MifareClassic.TYPE_PLUS:
                typeInfoString += "MifareClassic.TYPE_PLUS\n";
                break;
            case MifareClassic.TYPE_PRO:
                typeInfoString += "MifareClassic.TYPE_PRO\n";
                break;
            case MifareClassic.TYPE_CLASSIC:
                typeInfoString += "MifareClassic.TYPE_CLASSIC\n";
                break;
            case MifareClassic.TYPE_UNKNOWN:
                typeInfoString += "MifareClassic.TYPE_UNKNOWN\n";
                break;
            default:
                typeInfoString += "unknown...!\n";
        }

        int size = mifareClassicTag.getSize();
        switch(size){
            case MifareClassic.SIZE_1K:
                typeInfoString += "MifareClassic.SIZE_1K\n";
                break;
            case MifareClassic.SIZE_2K:
                typeInfoString += "MifareClassic.SIZE_2K\n";
                break;
            case MifareClassic.SIZE_4K:
                typeInfoString += "MifareClassic.SIZE_4K\n";
                break;
            case MifareClassic.SIZE_MINI:
                typeInfoString += "MifareClassic.SIZE_MINI\n";
                break;
            default:
                typeInfoString += "unknown size...!\n";
        }

        int blockCount = mifareClassicTag.getBlockCount();
        typeInfoString += "BlockCount \t= " + blockCount + "\n";
        int sectorCount = mifareClassicTag.getSectorCount();
        typeInfoString += "SectorCount \t= " + sectorCount + "\n";

        //textViewInfo.setText(typeInfoString);
        
        new ReadMifareClassicTask(mifareClassicTag).execute();
    }

    //public static byte[] readPage(MifareClassic tag, int page) {
    //    byte[] pageData = new byte[MifareClassic.BLOCK_SIZE];
    //    if (page > 0) {
    //        int sector = page/4;
    //        pageData = tag.readBlock(page);
    //        return pageData;
    //    }
    //    return new byte[0];
    //}

    enum CardType {
        UNKNOWN,
        MASTER_GET_STATE,
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

        static final int MASTER_CARD_SIGN         = 0xFF;
        static final int MASTER_CARD_GET_STATE    = 0xF9;
        static final int MASTER_CARD_SET_TIME     = 0xFA;
        static final int MASTER_CARD_SET_NUMBER   = 0xFB;
        static final int MASTER_CARD_SLEEP        = 0xFC;
        static final int MASTER_CARD_READ_BACKUP  = 0xFD;
        static final int MASTER_CARD_SET_PASS     = 0xFE;

        MifareClassic taskTag;
        boolean success;
        final int numOfSector = 16;
        final int numOfBlockInSector = 4;
        byte[][] buffer = new byte[numOfSector*numOfBlockInSector][MifareClassic.BLOCK_SIZE];
        CardType cardType = UNKNOWN;

        ReadMifareClassicTask(MifareClassic tag){
            taskTag = tag;
            success = false;
        }

        @Override
        protected void onPreExecute() {
            textViewTagInfo.setText("Reading Tag, don't remove it!");
        }

        protected byte[][] readBlocks(int firstBlockIndex, int count) {
            byte[][] blockData = new byte[count][MifareClassic.BLOCK_SIZE];
            try {
                if (!taskTag.isConnected()) {
                    taskTag.connect();
                }
                int lastSector = -1;
                int i = 0;
                for (int b = firstBlockIndex; b < (firstBlockIndex + count); ++b) {
                    int sector = taskTag.blockToSector(b);
                    if (sector != lastSector) {
                        lastSector = sector;
                        if (!taskTag.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                            return new byte[0][0];
                        }
                    }
                    blockData[i++] = taskTag.readBlock(b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return blockData;
        }

        @Override
        protected Void doInBackground(Void... params) {

            byte[] data = readBlocks(CARD_PAGE_INIT, 1)[0];
            if (data[2] == MASTER_CARD_SIGN && data[1] == MASTER_CARD_GET_STATE) {
                cardType = MASTER_GET_STATE;
                buffer = readBlocks(0, 12);
            }
            if(taskTag != null && taskTag.isConnected()){
                try {
                    taskTag.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            //try {
            //    taskTag.connect();

            //    for(int s=0; s<numOfSector; s++){
            //        if(taskTag.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT)) {
            //            for(int b=0; b<numOfBlockInSector; b++){
            //                int blockIndex = (s * numOfBlockInSector) + b;
            //                buffer[blockIndex] = taskTag.readBlock(blockIndex);
            //            }
            //        }
            //    }

            //    success = true;
            //} catch (IOException e) {
            //    e.printStackTrace();
            //} finally{
            //    if(taskTag!=null){
            //        try {
            //            taskTag.close();
            //        } catch (IOException e) {
            //            e.printStackTrace();
            //        }
            //    }
            //}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            switch(cardType) {
                case MASTER_GET_STATE:
                    Sportiduino.State state = new Sportiduino.State();
                    state.version = new Sportiduino.Version(buffer[8][0], buffer[8][1], buffer[8][2]);
                    state.config = Sportiduino.Config.unpack(buffer[9]);
                    state.battery = new Sportiduino.Battery(buffer[10][0]);
                    state.mode = buffer[10][1];

                    textViewTagInfo.setText(state.toString());
                    break;
                default:
                    textViewTagInfo.setText("Fail to read card!!!");
                    break;
            }
            //if(success){
            //    String stringBlock = "";
            //    for(int i=0; i<numOfSector; i++){
            //        stringBlock += i + " :\n";
            //        for(int j=0; j<numOfBlockInSector; j++){
            //            for(int k=0; k<MifareClassic.BLOCK_SIZE; k++){
            //                stringBlock += String.format("%02X", buffer[i][j][k] & 0xff) + " ";
            //            }
            //            stringBlock += "\n";
            //        }
            //        stringBlock += "\n";
            //    }
            //    textViewTagInfo.setText(stringBlock);
            //}else{
            //    textViewTagInfo.setText("Fail to read Blocks!!!");
            //}
        }
    }
}
