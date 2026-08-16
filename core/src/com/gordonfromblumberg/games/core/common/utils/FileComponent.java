package com.gordonfromblumberg.games.core.common.utils;

import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class FileComponent {
    private static final Logger log = LogManager.create(FileComponent.class);

    private final ByteBuffer buffer;

    public FileComponent(int bufferSize) {
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    // желательно вызывать не из основного потока
    public void save(File file, BinarySerializable data) {
        buffer.clear();
        data.write(buffer);
        buffer.flip();

        try (FileChannel out = FileChannel.open(file.toPath(), StandardOpenOption.CREATE,
                                                StandardOpenOption.WRITE,
                                                StandardOpenOption.TRUNCATE_EXISTING)) {
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
        } catch (IOException e) {
            log.error("Error while writing to " + file.getPath() + ": " + e.getMessage());
        }
    }

    // вызывать не из основного потока
    public void save(File file, ChunkedSerializable data, ProgressListener listener) {
        long totalBytes = data.getExpectedSize();
        long bytesWritten = 0;

        try (FileChannel out = FileChannel.open(file.toPath(), StandardOpenOption.CREATE,
                                                StandardOpenOption.WRITE,
                                                StandardOpenOption.TRUNCATE_EXISTING)) {
            while (data.hasMoreData()) {
                buffer.clear();
                data.writeNextChunk(buffer);
                buffer.flip();

                while (buffer.hasRemaining()) {
                    bytesWritten += out.write(buffer);
                }

                if (listener != null) {
                    listener.onProgress(bytesWritten, totalBytes);
                }
            }
        } catch (IOException e) {
            log.error("Error while writing to " + file.getPath() + ": " + e.getMessage());
        }
    }

    public void load(File file, BinarySerializable data) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file.getPath() + " does not exist");
        }

        buffer.clear();

        try (FileChannel in = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            while (in.read(buffer) != -1) {
                if (!buffer.hasRemaining()) {
                    throw new IllegalArgumentException("File " + file.getPath() + " is bigger than buffer");
                }
            }
            buffer.flip();
            data.read(buffer);
        } catch (IOException e) {
            log.error("Error while writing to " + file.getPath() + ": " + e.getMessage());
        }
    }

    public void load(File file, ChunkedSerializable data, ProgressListener listener) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file.getPath() + " does not exist");
        }

        buffer.clear();

        try (FileChannel in = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            long totalBytes = in.size(); // Общий размер файла для ProgressBar
            long bytesReadTotal = 0;

            // Читаем из канала, пока не дойдем до конца файла (read вернет -1)
            // или пока объект не скажет, что ему больше не нужны данные
            while (in.read(buffer) != -1 && !data.isLoadFinished()) {

                // Переключаем буфер в режим чтения объектом (limit = position, position = 0)
                buffer.flip();

                // Отдаем буфер объекту-строителю мира
                data.readNextChunk(buffer);

                // Подсчитываем прогресс
                bytesReadTotal += buffer.position();

                if (listener != null) {
                    listener.onProgress(bytesReadTotal, totalBytes);
                }

                // Компактизируем буфер. Если объект вычитал НЕ ВСЕ байты
                // (например, чанк не поместился целиком в остаток),
                // невычитанный "хвост" сдвигается в начало буфера,
                // а канал допишет новые данные в свободное место.
                buffer.compact();
            }
        } catch (IOException e) {
            log.error("Error while writing to " + file.getPath() + ": " + e.getMessage());
        }
    }
}
