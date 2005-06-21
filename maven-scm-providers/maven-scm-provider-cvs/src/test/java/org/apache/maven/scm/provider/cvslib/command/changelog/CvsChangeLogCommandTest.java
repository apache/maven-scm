package org.apache.maven.scm.provider.cvslib.command.changelog;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;
import org.codehaus.plexus.scm.ScmManager;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsChangeLogCommandTest extends AbstractCvsScmTest
{
    protected String getModule()
    {
        return "test-repo/changelog";
    }

    public void testGetCommandWithStartAndEndDate()
        throws Exception
    {
        Date startDate = getDate( 2003, 1, 1 );

        Date endDate = getDate( 2004, 1, 1 );

        testChangeLog( startDate, endDate, 32, null );
    }

    public void testGetCommandWithoutEndDate()
        throws Exception
    {
        Date startDate = getDate( 2003, 1, 1 );

        Date endDate = null;

        testChangeLog( startDate, endDate, 51, null );
    }

    public void testGetCommandWithBranchOrTag()
        throws Exception
    {
        Date startDate = null;

        Date endDate = null;

        testChangeLog( startDate, endDate, 22, "1.107.4" );
    }

    private void testChangeLog( Date startDate, Date endDate, int changeLogSize, String branch )
        throws Exception
    {
        ScmManager scmManager = getScmManager();

        CvsScmTestUtils.executeCVS( getWorkingDirectory(),
                                    "-f -d " + getTestFile( "src/test/repository/" ) + " co " + getModule() );

        ChangeLogScmResult changeLogResult = scmManager.changeLog( getScmRepository(), getScmFileSet(), startDate,
                                                                   endDate, 0, branch );

        if ( !changeLogResult.isSuccess() )
        {
            fail( changeLogResult.getProviderMessage() + "\n" + changeLogResult.getCommandOutput() );
        }

        List changeLog = changeLogResult.getChangeLog();

        assertNotNull( changeLog );

        assertEquals( changeLogSize, changeLog.size() );
    }
}
