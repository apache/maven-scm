package org.apache.maven.scm.tck.command.mkdir;

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

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;

/**
 * This test tests the mkdir command.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
public abstract class MkdirCommandTckTest
    extends ScmTckTestCase
{
    public void testMkdirCommand()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "missing/dir" ) );

        MkdirScmResult result = getScmManager().mkdir( getScmRepository(), fileSet, "Mkdir message" );

        assertResultIsSuccess( result );

        int revision = result.getRevision();

        assertTrue( revision > 0 );

        ListScmResult listResult = getScmManager().list( getScmRepository(), fileSet, true, null );

        assertTrue( "File should have been found.", listResult.isSuccess() );
    }
}
