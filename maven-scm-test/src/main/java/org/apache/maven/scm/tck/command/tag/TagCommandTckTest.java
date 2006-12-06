package org.apache.maven.scm.tck.command.tag;

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
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;

/**
 * This test tests the tag command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class TagCommandTckTest
    extends ScmTckTestCase
{

    public void testTagCommandTest()
        throws Exception
    {
        String tag = "test-tag";

        TagScmResult tagResult = getScmManager().getProviderByUrl( getScmUrl() )
            .tag( getScmRepository(), new ScmFileSet( getWorkingCopy() ), tag );

        assertResultIsSuccess( tagResult );

        assertEquals( "check all 4 files tagged", 4, tagResult.getTaggedFiles().size() );

        File readmeTxt = new File( getWorkingCopy(), "readme.txt" );

        assertEquals( "check readme.txt contents", "/readme.txt", FileUtils.fileRead( readmeTxt ) );

        changeReadmeTxt( readmeTxt );

        CheckInScmResult checkinResult = getScmManager().getProviderByUrl( getScmUrl() )
            .checkIn( getScmRepository(), new ScmFileSet( getWorkingCopy() ), null, "commit message" );

        assertResultIsSuccess( checkinResult );

        CheckOutScmResult checkoutResult = getScmManager().getProviderByUrl( getScmUrl() )
            .checkOut( getScmRepository(), new ScmFileSet( getAssertionCopy() ), null );

        assertResultIsSuccess( checkoutResult );

        readmeTxt = new File( getAssertionCopy(), "readme.txt" );

        assertEquals( "check readme.txt contents", "changed file", FileUtils.fileRead( readmeTxt ) );

        FileUtils.deleteDirectory( getAssertionCopy() );

        assertFalse( "check previous assertion copy deleted", getAssertionCopy().exists() );

        checkoutResult = getScmManager().getProviderByUrl( getScmUrl() )
            .checkOut( getScmRepository(), new ScmFileSet( getAssertionCopy() ), tag );

        assertResultIsSuccess( checkoutResult );

        assertEquals( "check readme.txt contents is from tagged version", "/readme.txt",
                      FileUtils.fileRead( readmeTxt ) );
    }

    private void changeReadmeTxt( File readmeTxt )
        throws Exception
    {
        FileWriter output = new FileWriter( readmeTxt );

        output.write( "changed file" );

        output.close();
    }
}
