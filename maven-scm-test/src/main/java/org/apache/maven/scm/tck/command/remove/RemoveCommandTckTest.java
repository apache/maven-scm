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
package org.apache.maven.scm.tck.command.remove;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** This test tests the remove command. */
public abstract class RemoveCommandTckTest extends ScmTckTestCase {
    @Test
    void testRemoveCommand() throws Exception {
        // existence has been tested in ScmTckTestCase.setup() already
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy(), "src/main/java/Application.java");
        RemoveScmResult removeResult = getScmManager().remove(getScmRepository(), fileSet, "remove1");

        assertResultIsSuccess(removeResult);
        // check removed files
        List<ScmFile> files = removeResult.getRemovedFiles();
        assertNotNull(files);
        assertEquals(1, files.size());
        ScmFile file1 = files.get(0);
        assertEquals(ScmFileStatus.DELETED, file1.getStatus());
        assertPath("src/main/java/Application.java", file1.getPath());

        // remove file with different basedir
        fileSet = new ScmFileSet(new File(getWorkingCopy(), "src"), new File("test/java/Test.java"));
        removeResult = getScmManager().remove(getScmRepository(), fileSet, "remove2");

        assertResultIsSuccess(removeResult);
        // check removed files
        files = removeResult.getRemovedFiles();
        assertNotNull(files);
        assertEquals(1, files.size());
        file1 = files.get(0);
        assertEquals(ScmFileStatus.DELETED, file1.getStatus());
        assertPath("test/java/Test.java", file1.getPath());

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Commit message");

        // checkin changes
        CheckInScmResult checkinResult =
                getScmManager().checkIn(getScmRepository(), new ScmFileSet(getWorkingCopy()), commandParameters);

        assertResultIsSuccess(checkinResult);

        // do a new checkout
        CheckOutScmResult checkoutResult =
                getScmManager().checkOut(getScmRepository(), new ScmFileSet(getAssertionCopy()));

        assertResultIsSuccess(checkoutResult);

        File applicationJava = new File(getAssertionCopy(), "src/main/java/Application.java");

        assertFalse(applicationJava.canRead(), "Application.java does exist even though it has been removed before");

        File testJava = new File(getAssertionCopy(), "src/test/java/Test.java");

        assertFalse(testJava.canRead(), "Test.java does exist even though it has been removed before");
    }
}
