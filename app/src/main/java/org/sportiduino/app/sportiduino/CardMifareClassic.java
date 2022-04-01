package org.sportiduino.app.sportiduino;

import android.nfc.tech.MifareClassic;
import android.util.Log;
//import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class CardMifareClassic extends CardAdapter {
    private final MifareClassic tag;
    final int numOfBlockInSector = 4;

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
        int lastSector = -1;
        int firstBlockIndex = firstPageIndex - 3 + (firstPageIndex - 3)/3;
        int i = 0;
        for (int blockIndex = firstBlockIndex; blockIndex < tag.getBlockCount(); ++blockIndex) {
            if ((blockIndex + 1) % numOfBlockInSector == 0) {
                continue;
            }
            int sector = tag.blockToSector(blockIndex);
            if (sector != lastSector) {
                lastSector = sector;
                try {
                    if (!tag.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                        Log.d("CardMifareClassic", "authenticateSectorWithKeyA failed, sector " +
                            sector + ", block " + 
                            blockIndex);
                        throw new ReadWriteCardException();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new ReadWriteCardException();
                }
            }
            try {
                blockData[i] = tag.readBlock(blockIndex);
                //Log.d("CardMifareClassic", i + ": " +
                //    Integer.toHexString(blockData[i][0] & 0xff) + " " +
                //    Integer.toHexString(blockData[i][1] & 0xff) + " " +
                //    Integer.toHexString(blockData[i][2] & 0xff) + " " +
                //    Integer.toHexString(blockData[i][3] & 0xff));
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
            ++i;
            if (i >= count) {
                break;
            }
        }
        return blockData;
    }

    public void writePages(int firstPageIndex, byte[][] data, int count) throws ReadWriteCardException {
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
                    //Log.d("mfc", String.format("authenticate sector: %d, blockIndex: %d, %b", sector, blockIndex, tag.isConnected()));
                    if (!tag.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                        throw new ReadWriteCardException();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new ReadWriteCardException();
                }
            }
            byte[] pageData = data[i++];
            if (pageData.length < MifareClassic.BLOCK_SIZE) {
                pageData = Arrays.copyOf(pageData, MifareClassic.BLOCK_SIZE);
            }
            try {
                //Log.d("mfc", String.format("writeBlock: %d, %b", blockIndex, tag.isConnected()));
                tag.writeBlock(blockIndex, pageData);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ReadWriteCardException();
            }
        }
    }

}
