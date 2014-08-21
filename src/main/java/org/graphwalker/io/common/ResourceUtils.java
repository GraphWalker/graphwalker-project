package org.graphwalker.io.common;

/*
 * #%L
 * GraphWalker Input/Output
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.String;
import java.lang.Thread;
import java.net.URL;

/**
 * @author Nils Olsson
 */
public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static File getResourceAsFile(final String filename) {
        File file = createFile(filename);
        if (file.exists()) {
            return file;
        } else {
            URL resource = ResourceUtils.class.getResource(filename);
            if (null == resource) {
                resource = Thread.currentThread().getContextClassLoader().getResource(filename);
            }
            if (null != resource) {
                return new File(resource.getFile());
            }
            throw new ResourceNotFoundException();
        }
    }

    public static InputStream getResourceAsStream(final String filename) {
        File file = createFile(filename);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ResourceNotFoundException();
            }
        } else {
            InputStream resource = ResourceUtils.class.getResourceAsStream(filename);
            if (null == resource) {
                resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            }
            if (null != resource) {
                return resource;
            }
            throw new ResourceNotFoundException();
        }
    }

    private static String[] splitPath(String filename) {
        return filename.split("[\\\\/]");
    }

    private static File createFile(String filename) {
        File createdFile = null;
        for (String part : splitPath(filename)) {
            createdFile = new File(createdFile, part);
        }
        return createdFile;
    }
}
