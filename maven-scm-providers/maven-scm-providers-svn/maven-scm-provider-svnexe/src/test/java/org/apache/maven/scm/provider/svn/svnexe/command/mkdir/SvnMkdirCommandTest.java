package org.apache.maven.scm.provider.svn.svnexe.command.mkdir;

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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
public class SvnMkdirCommandTest
    extends ScmTestCase
{
    private File messageFile;

    String messageFileString;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        messageFile = new File( "mkdir-message" );

        String path = messageFile.getAbsolutePath();        
        FileUtils.fileWrite( path, "create missing directory" );
        
        if ( path.indexOf( ' ' ) >= 0 )
        {
            path = "\"" + path + "\"";
        }
        messageFileString = "--file " + path;
    }
    
    protected void tearDown()
        throws Exception
    {
        assertTrue( messageFile.delete() );
        
        super.tearDown();
    }

    public void testCommandLineMkdirUrl()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk",
                         "svn --non-interactive mkdir http://foo.com/svn/trunk/missing " + messageFileString, false );
    }

    public void testCommandLineMkdirUrlWithUsername()
        throws Exception
    {
        testCommandLine( "scm:svn:http://anonymous@foo.com/svn/trunk",
                         "svn --username anonymous --no-auth-cache --non-interactive mkdir http://foo.com/svn/trunk/missing " +
                             messageFileString, false );
    }

    public void testCommandLineMkdirLocalPath()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "svn --non-interactive mkdir missing ", true );
    }

    private void testCommandLine( String scmUrl, String commandLine, boolean createInLocal )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/svn-mkdir-command-test" );

        ScmFileSet fileSet = new ScmFileSet( workingDirectory, new File( "missing" ) );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnMkdirCommand.createCommandLine( svnRepository, fileSet, messageFile, createInLocal );

        assertCommandLine( commandLine, workingDirectory, cl );
    }
}
