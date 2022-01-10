package org.sportiduino.app.sportiduino;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class CardMifareClassic extends Card {
    private final MifareClassic tag;
    final int numOfBlockInSector = 4;

    public CardMifareClassic(MifareClassic tag) {
        super(tag);
        this.tag = tag;
        int size = tag.getSize();
        switch (size) {
            case MifareClassic.SIZE_1K:
            case MifareClassic.SIZE_2K: // FIXME
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
        int firstBlockIndex = firstPageIndex - 3 + (firstPageIndex - 3)/3;
        int i = 0;
        for (int blockIndex = firstBlockIndex; i < count; ++blockIndex) {
            if ((blockIndex + 1) % numOfBlockInSector == 0) {
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
                blockData[i] = tag.readBlock(blockIndex);
                Log.d("Log", (firstBlockIndex + i) + ": "
                + Integer.toHexString(blockData[i][0] & 0xFF) + " "
                + Integer.toHexString(blockData[i][1] & 0xFF) + " "
                + Integer.toHexString(blockData[i][2] & 0xFF) + " "
                + Integer.toHexString(blockData[i][3] & 0xFF));
                ++i;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (stopIfPageNull && blockData[i][0] == 0) {
                break;
            }
        }
        return blockData;
    }

    public void writePages(int firstPageIndex, byte[][] data, int count) throws WriteCardException {
        int lastSector = -1;
        int firstBlockIndex = firstPageIndex - 3 + (firstPageIndex - 3)/3;
        int i = 0;
        for (int blockIndex = firstBlockIndex; i < count; ++blockIndex) {
            if ((blockIndex + 1) % numOfBlockInSector == 0) {
                continue;
            }
            int sector = tag.blockToSector(blockIndex);
            if (sector != lastSector) {
                lastSector = sector;
                try {
                    if (!tag.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                        throw new WriteCardException();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new WriteCardException();
                }
            }
            byte[] pageData = data[i++];
            if (pageData.length < MifareClassic.BLOCK_SIZE) {
                pageData = Arrays.copyOf(pageData, MifareClassic.BLOCK_SIZE);
            }
            try {
                tag.writeBlock(blockIndex, pageData);
            } catch (IOException e) {
                e.printStackTrace();
                throw new WriteCardException();
            }
        }
    }

}
