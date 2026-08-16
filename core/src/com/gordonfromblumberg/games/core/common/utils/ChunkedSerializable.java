package com.gordonfromblumberg.games.core.common.utils;

import java.nio.ByteBuffer;

public interface ChunkedSerializable {
    long getExpectedSize();
    boolean hasMoreData();
    void writeNextChunk(ByteBuffer buffer);
    void readNextChunk(ByteBuffer buffer);
    boolean isLoadFinished();
}
