package org.apache.maven.scm.provider.svn.command.changelog;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogCommandTest
    extends ScmTestCase
{
    public void testCommandLine()
        throws Exception
    {
        Calendar cal = Calendar.getInstance();

        cal.set(2003, 8, 10);

        Date startDate = cal.getTime();

        cal.set(2003, 9, 10);

        Date endDate = cal.getTime();

        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, startDate, endDate,
                         "svn log --non-interactive -v -r \"{2003/09/10 GMT}:{2003/10/10 GMT}\" http://foo.com/svn/trunk" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String tag, Date startDate, Date endDate, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/svn-update-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnChangeLogCommand.createCommandLine( svnRepository, workingDirectory, tag, startDate, endDate );

        assertEquals( commandLine, cl.toString() );
    }
}
