package org.apache.maven.scm.tck.command.list;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProvider;

import java.io.File;
import java.util.List;

/**
 * This test tests the list command.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class ListCommandTckTest
    extends ScmTckTestCase
{
    public void testListCommandTest()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( new File( "." ), new File( "." ) );

        List files = runList( fileSet, false );

        assertEquals( "The result of the list command doesn't have all the files in SCM: " + files, 3, files.size() );
    }

    public void testListCommandRecursiveTest()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( new File( "." ), new File( "." ) );

        List files = runList( fileSet, true );

        assertEquals( "The result of the list command doesn't have all the files in SCM: " + files, 10, files.size() );
    }

    public void testListCommandUnexistantFileTest()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( new File( "." ), new File( "/void" ) );

        ScmProvider provider = getScmManager().getProviderByUrl( getScmUrl() );

        ListScmResult result = provider.list( getScmRepository(), fileSet, false, null );

        assertFalse( "Found file when shouldn't", result.isSuccess() );
    }

    private List runList( ScmFileSet fileSet, boolean recursive )
        throws Exception
    {
        ScmProvider provider = getScmManager().getProviderByUrl( getScmUrl() );

        ListScmResult result = provider.list( getScmRepository(), fileSet, recursive, null );

        assertTrue( "Svn command failed: " + result.getCommandLine() + " : " + result.getCommandOutput(), result
            .isSuccess() );

        return result.getFiles();
    }
}
