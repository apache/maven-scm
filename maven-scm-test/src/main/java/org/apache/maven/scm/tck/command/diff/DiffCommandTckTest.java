package org.apache.maven.scm.tck.command.diff;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * This test tests the diff command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class DiffCommandTckTest
    extends ScmTckTestCase
{

    public void testDiffCommand()
        throws Exception
    {
        ScmRepository repository = getScmRepository();

        // ----------------------------------------------------------------------
        // Change the files
        // ----------------------------------------------------------------------

        //
        // readme.txt is changed (changed file in the root directory)
        // project.xml is added (added file in the root directory)
        // src/test/resources is untouched (a empty directory is left untouched)
        // src/test/java is untouched (a non empty directory is left untouched)
        // src/test/java/org (a empty directory is added)
        // src/main/java/org/Foo.java (a non empty directory is added)
        //

        // /readme.txt
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );

        // /project.xml
        ScmTestCase.makeFile( getWorkingCopy(), "/project.xml", "changed project.xml" );

        addToWorkingTree( getWorkingCopy(), new File( "project.xml" ), repository );

        // /src/test/java/org
        ScmTestCase.makeDirectory( getWorkingCopy(), "/src/test/java/org" );

        addToWorkingTree( getWorkingCopy(), new File( "src/test/java/org" ), repository );

        // /src/main/java/org/Foo.java
        ScmTestCase.makeFile( getWorkingCopy(), "/src/main/java/org/Foo.java" );

        addToWorkingTree( getWorkingCopy(), new File( "src/main/java/org" ), repository );

        // src/main/java/org/Foo.java
        addToWorkingTree( getWorkingCopy(), new File( "src/main/java/org/Foo.java" ), repository );

        // ----------------------------------------------------------------------
        // Diff the project
        // ----------------------------------------------------------------------

        ScmProvider provider = getScmManager().getProviderByUrl( getScmUrl() );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );
        DiffScmResult result = provider.diff( repository, fileSet, null, (ScmVersion) null );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        List changedFiles = result.getChangedFiles();

        Map differences = result.getDifferences();

        assertEquals( "Expected 3 files in the changed files list " + changedFiles, 3, changedFiles.size() );

        assertEquals( "Expected 3 files in the differences list " + differences, 3, differences.size() );

        // ----------------------------------------------------------------------
        // Assert the files in the changed files list
        // ----------------------------------------------------------------------

        Iterator files = new TreeSet( changedFiles ).iterator();

        //Check Foo.java
        ScmFile file = (ScmFile) files.next();

        assertPath( "/src/main/java/org/Foo.java", file.getPath() );

        assertTrue( file.getStatus().isDiff() );

        String postRangeStr = "+/src/main/java/org/Foo.java\n\\ No newline at end of file\n";
        String actualStr = differences.get( file.getPath() ).toString();
        assertTrue( actualStr.endsWith( postRangeStr ) );

        //Check readme.txt
        file = (ScmFile) files.next();

        assertPath( "/readme.txt", file.getPath() );

        assertTrue( file.getStatus().isDiff() );

        postRangeStr =
            "-/readme.txt\n\\ No newline at end of file\n+changed readme.txt\n\\ No newline at end of file\n";
        actualStr = differences.get( file.getPath() ).toString();
        assertTrue( actualStr.endsWith( postRangeStr ) );

        //Check project.xml
        file = (ScmFile) files.next();

        assertPath( "/project.xml", file.getPath() );

        postRangeStr = "+changed project.xml\n\\ No newline at end of file\n";
        actualStr = differences.get( file.getPath() ).toString();
        assertTrue( actualStr.endsWith( postRangeStr ) );

        assertTrue( file.getStatus().isDiff() );
    }
}
