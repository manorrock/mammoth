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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Manorrock Mammoth - JavaTest TCK to Maven.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class Mammoth {

    /**
     * Stores the Maven directory.
     */
    private File mavenDir = new File("maven");

    /**
     * Stores the TCK directory.
     */
    private File tckDir = new File("tck");

    /**
     * Stores the TCK URL.
     */
    private URL tckUrl;

    /**
     * Stores the TCK zip file.
     */
    private String tckZipFile = "tck.zip";

    /**
     * Stores the webapps directory.
     */
    private File webAppsDir = new File("webapps");

    /**
     * Create the Maven structure.
     */
    private void createMavenStructure() {
        try {
            // 0. create Maven dir if it does not exist.
            if (!mavenDir.exists()) {
                mavenDir.mkdirs();
            }
            // 1. create directories
            String[] filenames = webAppsDir.list();
            for (String filename : filenames) {
                File newFile = new File(mavenDir, filename.substring(0, filename.lastIndexOf(".war")));
                newFile.mkdirs();
            }
            // 2. create POMs for WARs
            File[] directories = mavenDir.listFiles();
            for (File directory : directories) {
                if (directory.isDirectory()) {
                    File pomFile = new File(directory, "pom.xml");
                    if (pomFile.createNewFile()) {
                        String content = """
<?xml version="1.0" encoding="UTF-8"?>
                                         
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 <modelVersion>4.0.0</modelVersion>
 <parent>
  <groupId>tck</groupId>
  <artifactId>project</artifactId>
  <version>1-SNAPSHOT</version>
 </parent>
 <artifactId>%s</artifactId>
 <packaging>war</packaging>
 <name>TCK - %s</name>
 <properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
 </properties>
</project>                                           
                                         """;
                        try ( FileWriter writer = new FileWriter(pomFile)) {
                            writer.write(String.format(
                                    content,
                                    directory.getName(),
                                    directory.getName()));
                            writer.flush();
                        }
                    }
                }
            }
            // 3. create top-level POM
            File topLevelPomFile = new File(mavenDir, "pom.xml");
            if (topLevelPomFile.createNewFile()) {
                StringBuilder modules = new StringBuilder();

                directories = mavenDir.listFiles();
                for (File directory : directories) {
                    modules.append("<module>").append(directory.getName()).append("</module>\n");
                }

                String content = """
<?xml version="1.0" encoding="UTF-8"?>
                                         
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 <modelVersion>4.0.0</modelVersion>
  <groupId>tck</groupId>
  <artifactId>project</artifactId>
  <version>1-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>TCK</name>
  <properties>
   <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <modules>
   %s
  </modules>
</project>                                           
                                         """;
                try ( FileWriter writer = new FileWriter(topLevelPomFile)) {
                    writer.write(String.format(
                            content,
                            modules.toString()));
                    writer.flush();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Deploy the wars.
     */
    private void deployWars() {
        try ( Stream<Path> walk = Files.walk(tckDir.toPath())) {

            List<File> files = walk
                    .map(Path::toFile)
                    .filter(file -> file.getName().toLowerCase().endsWith(".war"))
                    .collect(Collectors.toList());

            if (!webAppsDir.exists()) {
                webAppsDir.mkdirs();
            }

            files.forEach(file -> {
                File deployedFile = new File(webAppsDir, file.getName());
                if (!deployedFile.exists()) {
                    try {
                        Files.copy(file.toPath(), deployedFile.toPath());
                    } catch (IOException ioe) {
                        ioe.printStackTrace(System.err);
                    }
                } else {
                    System.err.println("Duplicate filename detected: " + file.getAbsolutePath());
                }
            });

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Download TCK.
     */
    private void downloadTck() {
        try ( InputStream stream = tckUrl.openStream()) {
            Files.copy(stream, Paths.get(tckZipFile));
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Extract TCK.
     */
    private void extractTck() {
        try ( ZipFile zipFile = new ZipFile(tckZipFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    File dir = new File(tckDir, entry.getName().substring(entry.getName().indexOf("/")));
                    dir.mkdirs();
                } else {
                    File file = new File(tckDir, entry.getName().substring(entry.getName().indexOf("/")));
                    Files.copy(zipFile.getInputStream(entry), file.toPath());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Run the program.
     */
    public void run() {
        downloadTck();
        extractTck();
        deployWars();
        createMavenStructure();
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
            if (arguments[i].equals("--tckDir")) {
                tckDir = new File(arguments[i + 1]);
            }
            if (arguments[i].equals("--webAppsDir")) {
                webAppsDir = new File(arguments[i + 1]);
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
