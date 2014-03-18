/*
 * Copyright (C) 2013 Maino
 * 
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
 * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
 * California, 94105, USA.
 * 
 */

package groovesquid.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteBuffer extends ByteArrayOutputStream {

    private volatile boolean closed;
    private final Object lock = new Object();

    public ByteBuffer(int size) {
        super(size);
    }

    public InputStream getInputStream() {
        return new ByteBufferInputStream();
    }

    @Override public void write(int b) {
        super.write(b);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override public void write(byte[] b) throws IOException {
        super.write(b);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override public void write(byte[] b, int off, int len) {
        super.write(b, off, len);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override public void close() throws IOException {
        closed = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }


    private class ByteBufferInputStream extends InputStream {
        private int pos = 0;

        @Override public int read() throws IOException {
            while (true) {
                if (isEof()) {
                    return -1;
                }
                if (pos < count) {
                    return buf[pos++];
                }
            }
        }

        @Override public int read(byte[] b, int off, int len) throws IOException {
            while (true) {
                if (isEof()) {
                    return -1;
                }
                int cnt = count;
                if (pos < cnt) {
                    if (pos + len > cnt)
                        len = cnt - pos;
                    System.arraycopy(buf, pos, b, off, len);
                    pos += len;
                    return len;
                }
            }
        }

        /**
         * Waits for next data, returns if data is available or eof reached.
         *
         * @return true on eof, false if more data is available
         * @throws IOException on i/o error
         */
        private boolean isEof() throws IOException {
            if (closed && pos >= count) {
                return true;
            }
            if (pos >= count) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                }
            }
            return closed && pos >= count;
        }

        @Override public int available() throws IOException {
            return count - pos;
        }

        @Override public void close() throws IOException {
            ByteBuffer.this.close();
        }
    }
}
