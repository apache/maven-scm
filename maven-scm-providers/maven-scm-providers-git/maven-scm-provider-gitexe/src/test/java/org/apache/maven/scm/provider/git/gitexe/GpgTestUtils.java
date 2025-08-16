/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider.git.gitexe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class GpgTestUtils {

    public static final String JOHN_DOE_KEY_FINGERPRINT = "DB91A1890A878E54C01ADEAC821EAC9D0567A97F";
    public static final String JOHN_DOE_KEY_LONG_ID = "821EAC9D0567A97F";
    public static final String JOHN_DOE_SECRET_KEY_RESOURCE_NAME = "/gpg/john-doe-secret-key.asc";
    public static final String BINARY_NAME = "gpg";

    private static Commandline createCommandline() {
        Commandline commandLine = new Commandline(BINARY_NAME);
        commandLine.createArg().setValue("--batch");
        return commandLine;
    }

    private static void execute(Commandline commandLine) throws GeneralSecurityException {
        try {
            int exitCode = CommandLineUtils.executeCommandLine(
                    commandLine,
                    new StreamConsumer() {
                        @Override
                        public void consumeLine(String line) {
                            // Handle output from the command if needed
                            System.out.println(line);
                        }
                    },
                    new StreamConsumer() {
                        @Override
                        public void consumeLine(String line) {
                            // Handle error output from the command if needed
                            System.err.println(line);
                        }
                    });
            if (exitCode != 0) {
                throw new GeneralSecurityException("GPG command failed with exit code: " + exitCode);
            }
        } catch (CommandLineException e) {
            throw new GeneralSecurityException("Failed to execute GPG command", e);
        }
    }

    /**
     * Imports a PGP key from a resource file.
     *
     * @param pgpKeyResourceName the name of the resource containing the PGP key
     * @throws IOException if an I/O error occurs while reading the resource
     */
    public static void importKey(String pgpKeyResourceName) throws IOException {
        Path tmpFile = Files.createTempFile("gpg-secret-key", ".key");
        try (InputStream input = GpgTestUtils.class.getResourceAsStream(pgpKeyResourceName)) {
            if (input == null) {
                throw new IOException("Secret GPG file not found: " + pgpKeyResourceName);
            }
            Files.copy(input, tmpFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            Commandline cmdLine = createCommandline();
            cmdLine.createArg().setValue("--import");
            cmdLine.createArg().setFile(tmpFile.toFile());
            execute(cmdLine);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        } finally {
            Files.delete(tmpFile);
        }
    }

    /**
     * Deletes a secret key by its fingerprint.
     *
     * @param fingerprint the fingerprint of the secret key to delete
     * @throws GeneralSecurityException if an error occurs while executing the command
     */
    public static void deleteSecretKey(String fingerprint) throws GeneralSecurityException {
        Commandline cmdLine = createCommandline();
        cmdLine.createArg().setValue("--yes");
        cmdLine.createArg().setValue("--delete-secret-key");
        cmdLine.createArg().setValue(fingerprint);
        execute(cmdLine);
    }
}
