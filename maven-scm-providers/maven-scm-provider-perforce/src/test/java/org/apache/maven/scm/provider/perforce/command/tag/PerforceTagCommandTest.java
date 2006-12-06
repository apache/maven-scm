package org.apache.maven.scm.provider.perforce.command.tag;

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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id: PerforceChangeLogCommandTest.java 162277 2005-01-11 19:02:26Z
 *          trygvis $
 */
public class PerforceTagCommandTest
    extends ScmTestCase
{
    public void testCreateCommandLine()
        throws Exception
    {
        testCommandLine( "foo-tag", "p4 label -i", "p4 labelsync -l foo-tag foo.xml bar.xml" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String tag, String create, String sync )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/perforce-tag-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname" );
        PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository
            .getProviderRepository();
        ScmFileSet files = new ScmFileSet( new File( "." ), new File[]{new File( "foo.xml" ), new File( "bar.xml" )} );

        Commandline cl1 = PerforceTagCommand.createLabelCommandLine( svnRepository, workingDirectory );
        assertEquals( create, cl1.toString() );
        Commandline cl2 = PerforceTagCommand.createLabelsyncCommandLine( svnRepository, workingDirectory, files, tag );
        assertEquals( sync, cl2.toString() );
    }
}
