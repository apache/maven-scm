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
package org.apache.maven.scm.provider.svn.svnexe.command.update;

import java.io.File;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.util.SvnUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnUpdateCommandTest extends ScmTestCase {
    @Test
    public void testCommandLineWithEmptyTag() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmTag(""),
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithEmptyBranch() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmBranch(""),
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithEmptyVersion() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmRevision(""),
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithWhitespaceTag() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmTag("  "),
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithWhitespaceBranch() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmBranch("  "),
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithWhitespaceRevision() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmRevision("  "),
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithoutTag() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                null,
                "svn --non-interactive update " + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineTag() throws Exception {
        testCommandLine(
                "scm:svn:http://anonymous@foo.com/svn/trunk",
                new ScmRevision("10"),
                "svn --username anonymous --no-auth-cache --non-interactive update -r 10 "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithUsernameAndTag() throws Exception {
        testCommandLine(
                "scm:svn:http://anonymous@foo.com/svn/trunk",
                new ScmRevision("10"),
                "svn --username anonymous --no-auth-cache --non-interactive update -r 10 "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithCygwinProperty() throws Exception {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            SvnUtil.setSettingsDirectory(getTestFile("src/test/resources/svn/update/cygwin"));
            try {
                assertTrue(SvnUtil.getSettings().isUseCygwinPath());
                testCommandLine(
                        "scm:svn:http://foo.com/svn/trunk",
                        null,
                        "svn --non-interactive update /mnt/c/my_working_directory@",
                        new File("c:\\my_working_directory"));
            } finally {
                SvnUtil.setSettingsDirectory(SvnUtil.DEFAULT_SETTINGS_DIRECTORY);
            }
        }
    }

    @Test
    public void testCommandLineWithRelativeURLTag() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmBranch("branches/my-test-branch"),
                "svn --non-interactive switch http://foo.com/svn/branches/my-test-branch@ "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithAbsoluteURLTag() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/trunk",
                new ScmBranch("http://foo.com/svn/branches/my-test-branch"),
                "svn --non-interactive switch http://foo.com/svn/branches/my-test-branch@ "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithNonDeterminantBase() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/some-project",
                new ScmBranch("branches/my-test-branch"),
                "svn --non-interactive switch http://foo.com/svn/some-project/branches/my-test-branch@ "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithNonDeterminantBaseTrailingSlash() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/some-project/",
                new ScmBranch("branches/my-test-branch"),
                "svn --non-interactive switch http://foo.com/svn/some-project/branches/my-test-branch@ "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    @Test
    public void testCommandLineWithBranchSameAsBase() throws Exception {
        testCommandLine(
                "scm:svn:http://foo.com/svn/tags/my-tag",
                new ScmTag("tags/my-tag"),
                "svn --non-interactive switch http://foo.com/svn/tags/my-tag@ "
                        + getUpdateTestFile().getAbsolutePath() + "@");
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private File getUpdateTestFile() {
        return getTestFile("target/svn-update-command-test");
    }

    private SvnScmProviderRepository getSvnRepository(String scmUrl) throws Exception {
        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        return (SvnScmProviderRepository) repository.getProviderRepository();
    }

    private void testCommandLine(String scmUrl, ScmVersion version, String commandLine) throws Exception {
        File workingDirectory = getUpdateTestFile();

        testCommandLine(scmUrl, version, commandLine, workingDirectory);
    }

    private void testCommandLine(String scmUrl, ScmVersion version, String commandLine, File workingDirectory)
            throws Exception {
        Commandline cl = SvnUpdateCommand.createCommandLine(getSvnRepository(scmUrl), workingDirectory, version, false);

        assertCommandLine(commandLine, workingDirectory, cl);
    }
}
