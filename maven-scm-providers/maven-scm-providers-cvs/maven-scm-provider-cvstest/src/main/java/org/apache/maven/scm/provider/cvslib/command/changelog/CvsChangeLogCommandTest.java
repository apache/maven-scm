package org.apache.maven.scm.provider.cvslib.command.changelog;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;

import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class CvsChangeLogCommandTest
    extends AbstractCvsScmTest
{
    /** {@inheritDoc} */
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

    @SuppressWarnings( "deprecation" )
    private void testChangeLog( Date startDate, Date endDate, int changeLogSize, String branch )
        throws Exception
    {
        if ( !isSystemCmd( CvsScmTestUtils.CVS_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( CvsScmTestUtils.CVS_COMMAND_LINE, getName() );
            return;
        }

        ScmManager scmManager = getScmManager();

        CvsScmTestUtils.executeCVS( getWorkingDirectory(),
                                    "-f -d " + getTestFile( "src/test/repository/" ) + " co " + getModule() );

        ChangeLogScmResult changeLogResult = scmManager.getProviderByRepository( getScmRepository() ).changeLog(
            getScmRepository(), getScmFileSet(), startDate, endDate, 0, branch );

        if ( !changeLogResult.isSuccess() )
        {
            fail( changeLogResult.getProviderMessage() + "\n" + changeLogResult.getCommandOutput() );
        }

        ChangeLogSet changeLogSet = changeLogResult.getChangeLog();

        assertNotNull( changeLogSet );

        assertEquals( changeLogSize, changeLogSet.getChangeSets().size() );
    }
}
