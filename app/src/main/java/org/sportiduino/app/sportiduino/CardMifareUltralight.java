package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_INIT;

import android.nfc.tech.MifareUltralight;
import android.nfc.tech.TagTechnology;
import android.util.Log;

import org.sportiduino.app.NtagAuthKey;

import java.io.IOException;
import java.util.Arrays;

public class CardMifareUltralight extends CardAdapter {
    private static final int PAGE_CFG0_OFFSET = 2;
    private static final int PAGE_CFG1_OFFSET = 3;
    private static final int PAGE_AUTH_KEY_OFFSET = 4;
    private static final int PAGE_PACK_OFFSET = 5;

    private final MifareUltralight tag;
    private NtagAuthKey authKey;
    private boolean authenticated = false;

    public CardMifareUltralight(MifareUltralight tag, NtagAuthKey authKey) {
        super((TagTechnology) tag);
        this.tag = tag;
        this.authKey = authKey;
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

    public boolean isNtag() {
        switch (tagType) {
            case NTAG213:
            case NTAG215:
            case NTAG216:
                return true;
        }
        return false;
    }

    public byte[][] readPages(int firstPageIndex, int count, boolean stopIfPageNull) throws ReadWriteCardException {
        if (isNtag()) {
            if (firstPageIndex + count - 1 >= CARD_PAGE_INIT && !authenticated) {
                ntagTryAuth();
            }
        }
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
        if (isNtag()) {
            if (!authenticated) {
                ntagTryAuth();
            }
        }
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

    @Override
    public void enableDisableAuthentication(boolean writeProtection, boolean readProtection) throws ReadWriteCardException {
        if (isNtag()) {
            if (writeProtection) {
                // Enable authentication from page 4 and with unlimited negative password verification attempts
                ntagSetPassword(readProtection, 0, (byte) CARD_PAGE_INIT);
            } else {
                disableAuthentication();
            }
        }
    }

    private void ntagSetPassword(boolean readAndWrite, int passwordAttempts, byte firstPage) throws ReadWriteCardException {
        if (authKey.isDefault()) {
            return;
        }
        passwordAttempts = Math.min(Math.max(passwordAttempts, 0), 7);

        int maxPage = getMaxPage();

        writePage(maxPage + PAGE_AUTH_KEY_OFFSET, authKey.toByteArray());

        byte[] packPageData = new byte[]{0, 0, 0, 0};
        // Write default PACK (0, 0)
        writePage(maxPage + PAGE_PACK_OFFSET, packPageData);

        byte accessByteData = readAndWrite ? (byte)0x80 : (byte)0x00;
        accessByteData |= (byte)passwordAttempts & 0x07;
        byte[] cfg1PageData = new byte[]{accessByteData, 0, 0, 0};
        // Write CFG1
        writePage(maxPage + PAGE_CFG1_OFFSET, cfg1PageData);

        // Set start page to enable the password verification at the end of the procedure
        byte[] cfg0PageData = new byte[]{0, 0, 0, firstPage};
        writePage(maxPage + PAGE_CFG0_OFFSET, cfg0PageData);
    }

    @Override
    public void disableAuthentication() throws ReadWriteCardException {
        int maxPage = getMaxPage();
        // Reset the authentication key
        writePage(maxPage + PAGE_AUTH_KEY_OFFSET, NtagAuthKey.defaultKey().toByteArray());

        byte[] cfg0PageData = new byte[]{0, 0, 0, (byte) 0xff};
        // Set start page of authentication to 0xff to disable authentication
        writePage(maxPage + PAGE_CFG0_OFFSET, cfg0PageData);
    }

    private void ntagTryAuth() {
        if (authKey.isDefault()) {
            return;
        }
        ntagAuth();  // ignore result
        authenticated = true;
    }

    private boolean ntagAuth() {
        Log.d("CardMifareUltralight", "Authenticating with key " + authKey);
        final byte[] EXPECTED_PACK = new byte[]{0x00, 0x00};
        // Build PWD_AUTH command
        byte[] pwdAuthCmd = new byte[5];
        pwdAuthCmd[0] = (byte) 0x1B; // PWD_AUTH
        System.arraycopy(authKey.toByteArray(), 0, pwdAuthCmd, 1, 4);

        byte[] result;
        try {
            // Send the command
            result = tag.transceive(pwdAuthCmd);
        } catch (IOException e) {
            e.printStackTrace();
            // If tag has no password written
            Log.d("CardMifareUltralight", "Auth failed, reconnect");
            close();
            try {
                connect();
            } catch (ReadWriteCardException ex) {
                ex.printStackTrace();
            }

            return false;
        }

        Log.d("CardMifareUltralight", "Auth response: " + Util.bytesToHex(result));
 
        // Expecting a 2-byte PACK response
        if (result != null && result.length == 2) {
            if (result[0] == EXPECTED_PACK[0] && result[1] == EXPECTED_PACK[1]) {
                return true; // Auth success
            }
        }
        return false;
    }
}

