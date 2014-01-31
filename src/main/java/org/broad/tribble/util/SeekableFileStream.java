/*
 * Copyright (c) 2007-2009 by The Broad Institute, Inc. and the Massachusetts Institute of Technology.
 * All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL), Version 2.1 which
 * is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR WARRANTIES OF
 * ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT
 * OR OTHER DEFECTS, WHETHER OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR
 * RESPECTIVE TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES OF
 * ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES, ECONOMIC
 * DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER THE BROAD OR MIT SHALL
 * BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT SHALL KNOW OF THE POSSIBILITY OF THE
 * FOREGOING.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.tribble.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author jrobinso
 */
public class SeekableFileStream extends SeekableStream {

    File file;
    FileInputStream fis;

    public SeekableFileStream(File file) throws FileNotFoundException {
        this.file = file;
        fis = new FileInputStream(file);
    }


    public boolean eof() throws IOException {
        return file.length() == fis.getChannel().position();
    }

    public long length() {
        return file.length();
    }


    public void seek(long position) throws IOException {
        fis.getChannel().position(position);
    }

    public long position() throws IOException {
        return fis.getChannel().position();
    }

    @Override
    public long skip(long n) throws IOException {
        return fis.skip(n);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return fis.read(buffer, offset, length);
    }

    @Override
    public int read() throws IOException {
        return fis.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return fis.read(b);    
    }

    @Override
    public int available() throws IOException {
        return fis.available();
    }

    @Override
    public void mark(int readlimit) {
        fis.mark(readlimit); 
    }

    @Override
    public boolean markSupported() {
        return fis.markSupported();
    }

    @Override
    public void reset() throws IOException {
        fis.reset();
    }

    @Override
    public void close() throws IOException {
        fis.close();

    }

}
