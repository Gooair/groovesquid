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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Store {

    // return a new instance of an OutputStream to write the store contents to
    OutputStream getOutputStream() throws IOException;

    // return a new instance of an InputStream to read the store contents
    InputStream getInputStream() throws IOException;

    void writeTrackInfo(Track track) throws IOException;

    void deleteStore();

    String getDescription();

    boolean isSameLocation(Store other);
}
