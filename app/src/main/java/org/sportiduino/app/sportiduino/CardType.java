package org.sportiduino.app.sportiduino;

import java.util.HashMap;
import java.util.Map;

public enum CardType {
    UNKNOWN(),
    ORDINARY(),
    MASTER_GET_STATE(0xf9),
    MASTER_SET_TIME(0xfa),
    MASTER_SET_NUMBER(0xfb),
    MASTER_SLEEP(0xfc),
    MASTER_READ_BACKUP(0xfd),
    MASTER_CONFIG(0xfe),
    MASTER_PASSWORD(0xff);

    private static final Map<Integer, CardType> BY_VALUE = new HashMap<>();

    static {
        for (CardType ct : values()) {
            BY_VALUE.put(ct.value, ct);
        }
    }

    public final int value;

    private CardType(int value) {
        this.value = value;
    }

    private CardType() {
        this(0);
    }

    public static CardType byValue(int value) {
        if (!BY_VALUE.containsKey(value)) {
            return UNKNOWN;
        }
        return BY_VALUE.get(value);
    }
}
