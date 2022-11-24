package org.sportiduino.app.sportiduino;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class CardMifareClassic extends CardAdapter {
    private final MifareClassic tag;
    final int numOfBlockInSector = 4;
    int lastSectorAuth = -1;

    public CardMifareClassic(MifareClassic tag) {
        super(tag);
        this.tag = tag;
        int size = tag.getSize();
        //Log.d("mfc", String.format("timeout: %d", tag.getTimeout()));
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

    public byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull) throws ReadWriteCardException {
        byte[][] blockData = new byte[count][MifareClassic.BLOCK_SIZE];
        int firstBlockIndex = firstPageIndex - 3 + (firstPageIndex - 3)/3;
        int i = 0;
        for (int blockIndex = firstBlockIndex; blockIndex < tag.getBlockCount(); ++blockIndex) {
            if ((blockIndex + 1) % numOfBlockInSector == 0) {
                continue;
            }
            int nAttempts = 3;
            for (int j = 0; j < nAttempts; ++j) {
                try {
                    authenticateSectorIfNeed(tag.blockToSector(blockIndex));
                    blockData[i] = tag.readBlock(blockIndex);
                    //Log.d("CardMifareClassic", i + ": " +
                    //    Integer.toHexString(blockData[i][0] & 0xff) + " " +
                    //    Integer.toHexString(blockData[i][1] & 0xff) + " " +
                    //    Integer.toHexString(blockData[i][2] & 0xff) + " " +
                    //    Integer.toHexString(blockData[i][3] & 0xff));
                } catch (IOException|ReadWriteCardException e) {
                    Log.d("mfc", e.toString());
                    if (j == nAttempts - 1) {
                        e.printStackTrace();
                        throw new ReadWriteCardException();
                    }
                }
            }
            if (stopIfPageNull
                && blockData[i][0] == 0
                && blockData[i][1] == 0
                && blockData[i][2] == 0
                && blockData[i][3] == 0) {
                break;
            }
            ++i;
            if (i >= count) {
                break;
            }
        }
        return blockData;
    }

    public void writePages(int firstPageIndex, byte[][] data, int count) throws ReadWriteCardException {
        int firstBlockIndex = firstPageIndex - 3 + (firstPageIndex - 3)/3;
        int i = 0;
        for (int blockIndex = firstBlockIndex; i < count; ++blockIndex) {
            if ((blockIndex + 1) % numOfBlockInSector == 0) {
                continue;
            }
            byte[] pageData = data[i++];
            if (pageData.length < MifareClassic.BLOCK_SIZE) {
                pageData = Arrays.copyOf(pageData, MifareClassic.BLOCK_SIZE);
            }

            int nAttempts = 5;
            for (int j = 0; j < nAttempts; ++j) {
                try {
                    if (!tag.isConnected()) {
                        connect();
                    }
                    authenticateSectorIfNeed(tag.blockToSector(blockIndex));
                    //Log.d("mfc", String.format("writeBlock: %d, %b", blockIndex, tag.isConnected()));
                    tag.writeBlock(blockIndex, pageData);
                    break;
                } catch (IOException|ReadWriteCardException e) {
                    Log.d("mfc", e.toString());
                    //Log.d("mfc", String.valueOf(j));
                    if (j == nAttempts - 1) {
                        e.printStackTrace();
                        throw new ReadWriteCardException();
                    }
                    lastSectorAuth = -1;
                    close();
                }
            }
        }
    }

    private void authenticateSectorIfNeed(int sector) throws ReadWriteCardException {
        if (sector != lastSectorAuth) {
            lastSectorAuth = sector;
            try {
                //Log.d("mfc", String.format("authenticate sector: %d, %b", sector, tag.isConnected()));
                if (!tag.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                    Log.d("CardMifareClassic", "authenticateSectorWithKeyA failed, sector " +
                        sector);
                    lastSectorAuth = -1;
                    throw new ReadWriteCardException();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new ReadWriteCardException();
            }
        }
    }
}
