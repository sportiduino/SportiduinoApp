package org.sportiduino.app.sportiduino;

import java.util.HashMap;
import java.util.Map;

public enum CardType {
    UNKNOWN(),
    ORDINARY(),
    MASTER_GET_STATE(0xF9),
    MASTER_SET_TIME(0xFA),
    MASTER_SET_NUMBER(0xFB),
    MASTER_SLEEP(0xFC),
    MASTER_READ_BACKUP(0xFD),
    MASTER_SET_PASS(0xFE);

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
