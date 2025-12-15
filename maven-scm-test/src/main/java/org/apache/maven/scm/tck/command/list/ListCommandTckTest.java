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
package org.apache.maven.scm.tck.command.list;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test tests the list command.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 */
public abstract class ListCommandTckTest extends ScmTckTestCase {
    @Test
    public void listCommandTest() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(new File("."), new File("."));

        List<ScmFile> files = runList(fileSet, false);

        assertEquals(3, files.size(), "The result of the list command doesn't have all the files in SCM: " + files);
    }

    @Test
    public void listCommandRecursiveTest() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(new File("."), new File("."));

        List<ScmFile> files = runList(fileSet, true);

        assertEquals(10, files.size(), "The result of the list command doesn't have all the files in SCM: " + files);
    }

    @Test
    public void listCommandUnexistantFileTest() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(new File("."), new File("/void"));

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());

        ListScmResult result = provider.list(getScmRepository(), fileSet, false, (ScmVersion) null);

        assertFalse(result.isSuccess(), "Found file when shouldn't");
    }

    private List<ScmFile> runList(ScmFileSet fileSet, boolean recursive) throws Exception {
        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());

        ListScmResult result = provider.list(getScmRepository(), fileSet, recursive, (ScmVersion) null);

        assertTrue(
                result.isSuccess(),
                "SCM command failed: " + result.getCommandLine() + " : " + result.getProviderMessage()
                        + (result.getCommandOutput() == null ? "" : ": " + result.getCommandOutput()));

        return result.getFiles();
    }
}
