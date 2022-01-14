package org.sportiduino.app.sportiduino;

import static org.sportiduino.app.sportiduino.Constants.CARD_PAGE_INIT;
import static org.sportiduino.app.sportiduino.Constants.FW_PROTO_VERSION;
import static org.sportiduino.app.sportiduino.Constants.MASTER_CARD_SIGN;

import org.sportiduino.app.Password;

public class MasterCard extends Card {
    final private byte[] password;

    public MasterCard(CardAdapter adapter, CardType type, Password password) {
        super(adapter);

        this.type = type;
        this.password = password.toByteArray();
    }

    @Override
    public byte[][] read() throws ReadWriteCardException {
        switch (type) {
            case MASTER_GET_STATE:
                return adapter.readPages(8, 12);
            default:
                return new byte[0][0];
        }
    }

    @Override
    public String parseData(byte[][] data) {
        switch (type) {
            case MASTER_SET_TIME:
                return "Time Master card";
            case MASTER_SET_NUMBER:
                return "Number Master card";
            case MASTER_GET_STATE:
                State state = new State(data);
                return "State Master card\n" + state.toString();
            case MASTER_SLEEP:
                return "Sleep Master card";
            case MASTER_READ_BACKUP:
                return "Backup Master card";
            case MASTER_SET_PASS:
                return "Password Master card";
            default:
                return "Unknown card type";
        }
    }

    @Override
    protected void writeImpl() throws ReadWriteCardException {
        final byte[][] header = {
                {0, (byte) type.value, MASTER_CARD_SIGN, FW_PROTO_VERSION},
                {password[0], password[1], password[2], 0}
        };
        adapter.writePages(CARD_PAGE_INIT, header, header.length);
        switch (type) {
            case MASTER_SET_NUMBER: {
                final byte[] data = {1, 0, 0, 0}; // FIXME
                adapter.writePage(6, data);
                break;
            }
            case MASTER_SLEEP: {
                // TODO: add wakeup time
                final byte[] data = {0, 0, 0, 0}; // FIXME
                adapter.writePage(6, data);
                break;
            }
            case MASTER_SET_TIME: {
                // FIXME: add time
                final byte[] data = {0, 0, 0, 0}; // FIXME
                adapter.writePage(6, data);
                break;
            }
        }
    }
}
