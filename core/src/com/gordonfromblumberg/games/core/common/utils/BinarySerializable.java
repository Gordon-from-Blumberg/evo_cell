package com.gordonfromblumberg.games.core.common.utils;

import java.nio.ByteBuffer;

public interface BinarySerializable {
    void write(ByteBuffer buffer);
    void read(ByteBuffer buffer);
    int estimateSize();
}
