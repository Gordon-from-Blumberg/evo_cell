package com.gordonfromblumberg.games.core.common.utils;

@FunctionalInterface
public interface ProgressListener {
    void onProgress(long bytesWritten, long totalBytes);
}
