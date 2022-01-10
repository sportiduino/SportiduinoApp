package org.sportiduino.app.sportiduino;

import android.nfc.tech.MifareUltralight;
import android.nfc.tech.TagTechnology;

import java.io.IOException;

public class CardMifareUltralight extends Card {
    private final MifareUltralight tag;

    public CardMifareUltralight(MifareUltralight tag) {
        super((TagTechnology) tag);
        this.tag = tag;
    }

    @Override
    public void connect() {
        super.connect();

        byte[] pageData = readPage(3);

        switch (pageData[2] & 0xFF) {
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

    public void writePages(int firstPageIndex, byte[][] data, int count) {
        int pageIndex = firstPageIndex;
        for (int i = 0; i < count; ++i) {
            try {
                tag.writePages(pageIndex++, data[i]);
            } catch (IOException e) {
                e.printStackTrace();
                throw new WriteCardException();
            }
        }
    }
}
