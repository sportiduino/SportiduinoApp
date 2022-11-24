package org.sportiduino.app.sportiduino;

import android.nfc.tech.TagTechnology;

import java.io.IOException;

public abstract class CardAdapter {
    protected TagTechnology tagTech;
    public TagType tagType = TagType.UNKNOWN;

    public CardAdapter(TagTechnology tagTech) {
        this.tagTech = tagTech;
    }

    public void connect() throws ReadWriteCardException {
        try {
            if (!tagTech.isConnected()) {
                tagTech.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ReadWriteCardException();
        }
    }

    public void close() {
        if (tagTech != null && tagTech.isConnected()) {
            try {
                tagTech.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull) throws ReadWriteCardException;

    public byte[][] readPages(int firstPageIndex, int count) throws ReadWriteCardException{
        return readPages(firstPageIndex, count, false);
    }

    public byte[] readPage(int firstPageIndex) throws ReadWriteCardException {
        return readPages(firstPageIndex, 1)[0];
    }

    public abstract void writePages(int firstPageIndex, byte[][] data, int count) throws ReadWriteCardException;

    public void writePage(int firstPageIndex, byte[] data) throws ReadWriteCardException {
        writePages(firstPageIndex, new byte[][]{data}, 1);
    }

    public int getMaxPage() {
        switch (tagType) {
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

    public void clearPage(int page) throws ReadWriteCardException {
        final byte[] data = {0, 0, 0, 0};
        writePage(page, data);
    }

    public void clear(int beginPage, int endPage) throws ReadWriteCardException {
        for (int page = endPage; page >= beginPage; --page) {
            clearPage(page);
        }
    }
}
