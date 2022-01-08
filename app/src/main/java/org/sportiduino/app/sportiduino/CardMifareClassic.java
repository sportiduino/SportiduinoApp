package org.sportiduino.app.sportiduino;

import android.nfc.tech.MifareClassic;
import android.nfc.tech.TagTechnology;

import java.io.IOException;

public class CardMifareClassic extends Card {
    private final MifareClassic tag;
    final int numOfBlockInSector = 4;

    public CardMifareClassic(MifareClassic tag) {
        super((TagTechnology) tag);
        this.tag = tag;
        int size = tag.getSize();
        switch (size) {
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
        int firstBlockIndex = firstPageIndex - 3 + ((firstPageIndex - 3) / 3);
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
