package org.sportiduino.app.sportiduino;

import android.nfc.tech.MifareUltralight;
import android.nfc.tech.TagTechnology;

import java.io.IOException;
import java.util.Arrays;

public class CardMifareUltralight extends CardAdapter {
    private final MifareUltralight tag;

    public CardMifareUltralight(MifareUltralight tag) {
        super((TagTechnology) tag);
        this.tag = tag;
    }

    @Override
    public void connect() throws ReadWriteCardException {
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

    public byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull) throws ReadWriteCardException {
        byte[][] blockData = new byte[count][16];
        int pageIndex = firstPageIndex;
        for (int i = 0; i < count; ++i) {
            try {
                blockData[i] = tag.readPages(pageIndex++);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ReadWriteCardException();
            }
            if (stopIfPageNull
                && blockData[i][0] == 0
                && blockData[i][1] == 0
                && blockData[i][2] == 0
                && blockData[i][3] == 0) {
                break;
            }
        }
        return blockData;
    }

    public void writePages(int firstPageIndex, byte[][] data, int count) throws ReadWriteCardException {
        int pageIndex = firstPageIndex;
        for (int i = 0; i < count; ++i) {
            byte[] pageData = data[i];
            if (pageData.length < MifareUltralight.PAGE_SIZE) {
                pageData = Arrays.copyOf(pageData, MifareUltralight.PAGE_SIZE);
            }
            try {
                tag.writePage(pageIndex++, pageData);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ReadWriteCardException();
            }
        }
    }
}
