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

package groovesquid.model;

import groovesquid.util.ByteBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.farng.mp3.MP3File;


public class MemoryStore implements Store {
    private final String description;
    private final ByteBuffer buf = new ByteBuffer(2 * 1024 * 1024);

    public MemoryStore(String description) {
        this.description = description;
    }

    @Override public OutputStream getOutputStream() throws IOException {
        return buf;
    }

    @Override public InputStream getInputStream() throws IOException {
        return buf.getInputStream();
    }
    
    @Override public void writeTrackInfo(Track track) {
    }

    @Override public void deleteStore() {
    }

    @Override public String getDescription() {
        return "mem{size=" + buf.size() + ", " + description + "}";
    }

    @Override public boolean isSameLocation(Store other) {
        return other instanceof MemoryStore;
    }

}