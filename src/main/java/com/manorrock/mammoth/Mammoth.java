/*
 * Copyright (c) 2002-2021 Manorrock.com. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.manorrock.mammoth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manorrock Mammoth - JavaTest TCK to Maven.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class Mammoth {

    /**
     * Stores the TCK URL.
     */
    private URL tckUrl;

    /**
     * Stores the TCK zip file.
     */
    private String tckZipFile = "tck.zip";

    /**
     * Download TCK.
     */
    public void downloadTck() {
        try (InputStream stream = tckUrl.openStream()) {
            Files.copy(stream, Paths.get(tckZipFile));
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Run the program.
     */
    public void run() {
        downloadTck();
    }

    /**
     * Parse the arguments.
     *
     * @param arguments the arguments.
     * @return the program.
     */
    public Mammoth parseArguments(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("--tckUrl")) {
                try {
                    tckUrl = new URL(arguments[i + 1]);
                } catch (MalformedURLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        return this;
    }

    /**
     * Main method.
     *
     * @param arguments the command-line arguments.
     */
    public static void main(String[] arguments) {
        new Mammoth().parseArguments(arguments).run();
    }
}
