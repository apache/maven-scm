package org.apache.maven.scm.provider.svn.command.update;

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

import org.apache.maven.scm.tck.command.update.UpdateCommandTckTest;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnUpdateCommandTckTest
    extends UpdateCommandTckTest
{
    private final static File updateDump = getTestFile( "src/test/resources/tck/update.dump" );

    public String getScmUrl()
        throws Exception
    {
        return "scm:svn:file://" + getRepositoryRoot();
    }

    public void initRepo()
        throws Exception
    {
        SvnScmTestUtils.initializeRepository( getRepositoryRoot(), updateDump );
    }

    public void checkOut( File workingDirectory )
        throws Exception
    {
        execute( workingDirectory.getParentFile(), "svn", "checkout file://" + getRepositoryRoot() + " " + workingDirectory.getName() );
    }

    public void addFileToRepository( File workingDirectory, String file )
        throws Exception
    {
        execute( workingDirectory, "svn", "add " + file );
    }

    public void addDirectoryToRepository( File workingDirectory, String directory )
        throws Exception
    {
        execute( workingDirectory, "svn", "add " + directory );
    }

    public void commit( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        execute( workingDirectory, "svn", "commit -m '' --non-interactive" );
    }
}
