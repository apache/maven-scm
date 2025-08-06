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
package org.apache.maven.scm;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author dtran
 */
public class ScmFileSetTest {
    private static String basedirPath;

    public static String getBasedir() {
        if (basedirPath != null) {
            return basedirPath;
        }

        basedirPath = System.getProperty("basedir");

        if (basedirPath == null) {
            basedirPath = new File("").getAbsolutePath();
        }

        return basedirPath;
    }

    private String removeBasedir(String filename) {
        return filename.substring(getBasedir().length(), filename.length());
    }

    @Test
    public void testFilesList() throws IOException {
        ScmFileSet fileSet = new ScmFileSet(new File(getBasedir(), "src"), "**/**");
        assertEquals("src", fileSet.getBasedir().getName());
        assertEquals("**/**", fileSet.getIncludes());
        // assertEquals( ScmFileSet.DEFAULT_EXCLUDES, fileSet.getExcludes() );
        assertTrue(
                "List of files should be longer than 10 elements, but received: "
                        + fileSet.getFileList().size(),
                fileSet.getFileList().size() > 10);
    }

    @Test
    public void testFilesListWithoutIncludesResultsEmptyList() throws IOException {
        ScmFileSet fileSet = new ScmFileSet(new File(getBasedir(), "src"));
        assertEquals(0, fileSet.getFileList().size());
    }

    @Test
    public void testFilesListExcludes() throws IOException {
        ScmFileSet fileSet = new ScmFileSet(new File(getBasedir(), "src"), "**/**", "**/exclude/**");

        List<File> files = fileSet.getFileList();

        Iterator<File> it = files.iterator();
        while (it.hasNext()) {
            File file = it.next();
            if (removeBasedir(file.getAbsolutePath()).indexOf("exclude") != -1) {
                fail("Found excludes in file set: " + file);
            }
        }
    }

    @Test
    public void testFilesListExcludes2() throws IOException {
        ScmFileSet fileSet = new ScmFileSet(new File(getBasedir(), "src"), "**/scmfileset/**", "**/exclude/**");

        assertEquals(2, fileSet.getFileList().size());
    }

    @Test
    public void testFilesListNoExcludes() throws IOException {
        ScmFileSet fileSet = new ScmFileSet(new File(getBasedir(), "src"), "**/scmfileset/**");

        assertEquals(4, fileSet.getFileList().size());
    }
}
