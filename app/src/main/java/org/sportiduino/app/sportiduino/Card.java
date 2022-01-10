package org.sportiduino.app.sportiduino;

import android.nfc.tech.TagTechnology;

import java.io.IOException;


public abstract class Card {
    protected TagTechnology tagTech;
    public TagType tagType = TagType.UNKNOWN;
    public CardType type = CardType.UNKNOWN;

    public Card(TagTechnology tagTech) {
        this.tagTech = tagTech;
    }

    public void connect() {
        try {
            if (!tagTech.isConnected()) {
                tagTech.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if(tagTech != null && tagTech.isConnected()){
            try {
                tagTech.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull);

    public byte[][] readPages(int firstPageIndex, int count) {
        return readPages(firstPageIndex, count, false);
    }

    public byte[] readPage(int firstPageIndex) {
        return readPages(firstPageIndex, 1)[0];
    }

    public abstract void writePages(int firstPageIndex, byte[][] data, int count) throws WriteCardException;

    public void writePage(int firstPageIndex, byte[][] data) {
        writePages(firstPageIndex, data, 1);
    }

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

public class WriteCardException extends Exception {
    public WriteCardException(String errorMsg) {
        super(errorMsg);
    }

    public WriteCardException() {
        super();
    }
}

