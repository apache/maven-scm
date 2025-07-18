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
package org.apache.maven.scm.tck.command.checkin;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.util.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This test tests the check out command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class CheckInCommandTckTest extends ScmTckTestCase {
    @Test
    public void testCheckInCommandTest() throws Exception {
        // Make sure that the correct files was checked out
        File fooJava = new File(getWorkingCopy(), "src/main/java/Foo.java");

        File barJava = new File(getWorkingCopy(), "src/main/java/Bar.java");

        File readmeTxt = new File(getWorkingCopy(), "readme.txt");

        assertFalse("check Foo.java doesn't yet exist", fooJava.canRead());

        assertFalse("check Bar.java doesn't yet exist", barJava.canRead());

        assertTrue("check can read readme.txt", readmeTxt.canRead());

        // Change the files
        createFooJava(fooJava);

        createBarJava(barJava);

        changeReadmeTxt(readmeTxt);

        AddScmResult addResult = getScmManager()
                .add(getScmRepository(), new ScmFileSet(getWorkingCopy(), "src/main/java/Foo.java", null));

        assertResultIsSuccess(addResult);

        List<ScmFile> files = addResult.getAddedFiles();
        assertNotNull(files);
        assertEquals(1, files.size());
        // SCM-998: filename separators not yet harmonized
        assertEquals(
                "src/main/java/Foo.java",
                FilenameUtils.normalizeFilename(files.get(0).getPath()));

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Commit message");
        commandParameters.setString(CommandParameter.SCM_COMMIT_SIGN, "false");

        CheckInScmResult result =
                getScmManager().checkIn(getScmRepository(), new ScmFileSet(getWorkingCopy()), commandParameters);

        assertResultIsSuccess(result);

        files = result.getCheckedInFiles();
        assertNotNull(files);
        assertEquals(2, files.size());

        Map<String, ScmFile> fileMap = mapFilesByPath(files);
        ScmFile file1 = fileMap.get("src/main/java/Foo.java");
        assertNotNull(file1);
        assertEquals(ScmFileStatus.CHECKED_IN, file1.getStatus());

        ScmFile file2 = fileMap.get("readme.txt");
        assertNotNull(file2);
        assertEquals(ScmFileStatus.CHECKED_IN, file2.getStatus());

        CheckOutScmResult checkoutResult =
                getScmManager().checkOut(getScmRepository(), new ScmFileSet(getAssertionCopy()));

        assertResultIsSuccess(checkoutResult);

        fooJava = new File(getAssertionCopy(), "src/main/java/Foo.java");

        barJava = new File(getAssertionCopy(), "src/main/java/Bar.java");

        readmeTxt = new File(getAssertionCopy(), "readme.txt");

        assertTrue("check can read Foo.java", fooJava.canRead());

        assertFalse("check Bar.java doesn't exist", barJava.canRead());

        assertTrue("check can read readme.txt", readmeTxt.canRead());

        assertEquals("check readme.txt contents", "changed file", FileUtils.fileRead(readmeTxt));
    }

    @Test
    public void testCheckInCommandPartialFileset() throws Exception {
        // Make sure that the correct files was checked out
        File fooJava = new File(getWorkingCopy(), "src/main/java/Foo.java");

        File barJava = new File(getWorkingCopy(), "src/main/java/Bar.java");

        File readmeTxt = new File(getWorkingCopy(), "readme.txt");

        assertFalse("check Foo.java doesn't yet exist", fooJava.canRead());

        assertFalse("check Bar.java doesn't yet exist", barJava.canRead());

        assertTrue("check can read readme.txt", readmeTxt.canRead());

        // Change the files
        createFooJava(fooJava);

        createBarJava(barJava);

        changeReadmeTxt(readmeTxt);

        AddScmResult addResult = getScmManager()
                .getProviderByUrl(getScmUrl())
                .add(getScmRepository(), new ScmFileSet(getWorkingCopy(), "src/main/java/Foo.java", null));

        assertResultIsSuccess(addResult);

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Commit message");
        commandParameters.setString(CommandParameter.SCM_COMMIT_SIGN, "false");

        CheckInScmResult result = getScmManager()
                .checkIn(getScmRepository(), new ScmFileSet(getWorkingCopy(), "**/Foo.java", null), commandParameters);

        assertResultIsSuccess(result);

        List<ScmFile> files = result.getCheckedInFiles();
        assertNotNull(files);
        assertEquals(1, files.size());

        ScmFile file1 = files.get(0);
        assertEquals(ScmFileStatus.CHECKED_IN, file1.getStatus());
        assertPath("src/main/java/Foo.java", file1.getPath());

        CheckOutScmResult checkoutResult =
                getScmManager().checkOut(getScmRepository(), new ScmFileSet(getAssertionCopy()));

        assertResultIsSuccess(checkoutResult);

        fooJava = new File(getAssertionCopy(), "src/main/java/Foo.java");

        barJava = new File(getAssertionCopy(), "src/main/java/Bar.java");

        readmeTxt = new File(getAssertionCopy(), "readme.txt");

        assertTrue("check can read Foo.java", fooJava.canRead());

        assertFalse("check Bar.java doesn't exist", barJava.canRead());

        assertTrue("check can read readme.txt", readmeTxt.canRead());

        assertEquals("check readme.txt contents", "/readme.txt", FileUtils.fileRead(readmeTxt));
    }

    @Test
    public void testCheckInCommandFilesetWithBasedirOtherThanWorkingCopyRoot() throws Exception {
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());

        Assume.assumeFalse(
                "Local provider does not properly support basedir",
                scmProvider.getScmType().equals("local"));
        // Make sure that the correct files was checked out
        File fooJava = new File(getWorkingCopy(), "src/main/java/Foo.java");

        assertFalse("check Foo.java doesn't yet exist", fooJava.canRead());

        // Change the files
        createFooJava(fooJava);

        AddScmResult addResult = scmProvider.add(
                getScmRepository(), new ScmFileSet(new File(getWorkingCopy(), "src"), "main/java/Foo.java", null));

        assertResultIsSuccess(addResult);

        List<ScmFile> files = addResult.getAddedFiles();
        assertNotNull(files);
        assertEquals(1, files.size());
        // SCM-998: filename separators not yet harmonized
        assertEquals(
                "main/java/Foo.java",
                FilenameUtils.normalizeFilename(files.get(0).getPath()));

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Commit message");
        commandParameters.setString(CommandParameter.SCM_COMMIT_SIGN, "false");

        CheckInScmResult result = getScmManager()
                .checkIn(
                        getScmRepository(),
                        new ScmFileSet(new File(getWorkingCopy(), "src"), "**/Foo.java", null),
                        commandParameters);

        assertResultIsSuccess(result);

        files = result.getCheckedInFiles();
        assertNotNull(files);
        assertEquals(1, files.size());

        ScmFile file1 = files.get(0);
        assertEquals(ScmFileStatus.CHECKED_IN, file1.getStatus());
        assertPath("main/java/Foo.java", file1.getPath());

        CheckOutScmResult checkoutResult =
                getScmManager().checkOut(getScmRepository(), new ScmFileSet(getAssertionCopy()));

        assertResultIsSuccess(checkoutResult);

        fooJava = new File(getAssertionCopy(), "src/main/java/Foo.java");

        assertTrue("check can read Foo.java", fooJava.canRead());
    }

    private void createFooJava(File fooJava) throws Exception {
        try (PrintWriter printer = new PrintWriter(new FileWriter(fooJava))) {
            printer.println("public class Foo");
            printer.println("{");

            printer.println("    public void foo()");
            printer.println("    {");
            printer.println("        int i = 10;");
            printer.println("    }");

            printer.println("}");
        }
    }

    private void createBarJava(File barJava) throws Exception {
        try (PrintWriter printer = new PrintWriter(new FileWriter(barJava))) {
            printer.println("public class Bar");
            printer.println("{");

            printer.println("    public int bar()");
            printer.println("    {");
            printer.println("        return 20;");
            printer.println("    }");

            printer.println("}");
        }
    }

    private void changeReadmeTxt(File readmeTxt) throws Exception {
        try (FileWriter output = new FileWriter(readmeTxt)) {
            output.write("changed file");
        }
    }
}
