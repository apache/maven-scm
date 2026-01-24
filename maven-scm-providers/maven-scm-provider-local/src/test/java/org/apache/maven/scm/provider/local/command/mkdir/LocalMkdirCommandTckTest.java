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
package org.apache.maven.scm.provider.local.command.mkdir;

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.tck.command.mkdir.MkdirCommandTckTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 *
 */
public class LocalMkdirCommandTckTest extends MkdirCommandTckTest {
    private static final String MODULE_NAME = "checkin-tck";

    public String getScmUrl() {
        return "scm:local|" + getRepositoryRoot() + "|" + MODULE_NAME;
    }

    public void initRepo() throws Exception {
        makeRepo(getRepositoryRoot());
    }

    private void makeRepo(File workingDirectory) throws Exception {
        makeFile(workingDirectory, MODULE_NAME + "/pom.xml", "/pom.xml");

        makeFile(workingDirectory, MODULE_NAME + "/readme.txt", "/readme.txt");

        makeFile(workingDirectory, MODULE_NAME + "/src/main/java/Application.java", "/src/main/java/Application.java");

        makeFile(workingDirectory, MODULE_NAME + "/src/test/java/Test.java", "/src/test/java/Test.java");

        makeDirectory(workingDirectory, MODULE_NAME + "/src/test/resources");
    }

    @Test
    void testMkdirCommandMkdirUrl() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy(), new File(getMissingDirectory()));

        MkdirScmResult result = getScmManager().mkdir(getScmRepository(), fileSet, "Mkdir message", false);

        assertResultIsSuccess(result);

        ListScmResult listResult = getScmManager().list(getScmRepository(), fileSet, true, null);

        assertTrue(listResult.isSuccess(), "Directory should have been found.");
    }

    @Test
    void testMkdirCommandDirAlreadyAdded() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy(), new File(getMissingDirectory()));

        MkdirScmResult result = getScmManager().mkdir(getScmRepository(), fileSet, "Mkdir message", false);

        assertResultIsSuccess(result);

        ListScmResult listResult = getScmManager().list(getScmRepository(), fileSet, true, null);

        assertTrue(listResult.isSuccess(), "Directory should have been found.");

        // add the directory again
        result = getScmManager().mkdir(getScmRepository(), fileSet, "Mkdir message", false);

        assertFalse(result.isSuccess());

        printOutputError(result);
    }
}
