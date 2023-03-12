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
package org.apache.maven.scm.provider.git;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.scm.PlexusJUnit4TestSupport;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Assert;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public final class GitScmTestUtils {
    /** 'git' command line */
    public static final String GIT_COMMAND_LINE = "git";

    private GitScmTestUtils() {}

    public static void initRepo(File repository, File workingDirectory, File assertionDirectory) throws IOException {
        initRepo("src/test/repository/", repository, workingDirectory);

        FileUtils.deleteDirectory(assertionDirectory);

        Assert.assertTrue(assertionDirectory.mkdirs());
    }

    public static void initRepo(String source, File repository, File workingDirectory) throws IOException {
        // Copy the repository to target
        File src = PlexusJUnit4TestSupport.getTestFile(source);

        FileUtils.deleteDirectory(repository);

        Assert.assertTrue(repository.mkdirs());

        FileUtils.copyDirectoryStructure(src, repository);

        File dotGitDirectory = new File(src, "dotgit");

        if (dotGitDirectory.exists()) {
            FileUtils.copyDirectoryStructure(dotGitDirectory, new File(repository, ".git"));
        }

        FileUtils.deleteDirectory(workingDirectory);

        Assert.assertTrue(workingDirectory.mkdirs());
    }

    public static String getScmUrl(File repositoryRootFile, String provider) throws CommandLineException {
        return "scm:" + provider + ":"
                + repositoryRootFile.toPath().toAbsolutePath().toUri().toASCIIString();
    }

    public static void deleteAllDirectories(File startDirectory, String pattern) throws IOException {
        if (startDirectory.isDirectory()) {
            File[] childs = startDirectory.listFiles();
            for (int i = 0; i < childs.length; i++) {
                File child = childs[i];
                if (child.isDirectory()) {
                    if (child.getName().equals(pattern)) {
                        FileUtils.deleteDirectory(child);
                    } else {
                        deleteAllDirectories(child, pattern);
                    }
                }
            }
        }
    }

    public static void setDefaultUser(File repositoryRootFile) {
        File gitConfigFile = new File(new File(repositoryRootFile, ".git"), "config");

        FileWriter fw = null;
        try {
            fw = new FileWriter(gitConfigFile, true);
            fw.append("[user]\n");
            fw.append("\tname = John Doe\n");
            fw.append("\temail = john.doe@nowhere.com\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            System.err.println("cannot setup a default user for tests purpose inside " + gitConfigFile);
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ignore) {
                    // ignored
                }
            }
        }
    }
}
