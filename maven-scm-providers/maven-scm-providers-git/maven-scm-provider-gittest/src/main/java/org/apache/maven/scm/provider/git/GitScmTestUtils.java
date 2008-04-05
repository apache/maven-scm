package org.apache.maven.scm.provider.git;

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

import junit.framework.Assert;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public final class GitScmTestUtils
{
    private GitScmTestUtils()
    {
    }

    public static void initRepo( File repository, File workingDirectory, File assertionDirectory )
        throws IOException
    {
        initRepo( "src/test/repository/", repository, workingDirectory );
    
        FileUtils.deleteDirectory( assertionDirectory );
    
        Assert.assertTrue( assertionDirectory.mkdirs() );
    }

    public static void initRepo( String source, File repository, File workingDirectory )
        throws IOException
    {
        // Copy the repository to target
        File src = PlexusTestCase.getTestFile( source );
    
        FileUtils.deleteDirectory( repository );
    
        Assert.assertTrue( repository.mkdirs() );
    
        FileUtils.copyDirectoryStructure( src, repository );
    
        FileUtils.deleteDirectory( workingDirectory );
    
        Assert.assertTrue( workingDirectory.mkdirs() );
    }

    public static String getScmUrl( File repositoryRootFile )
        throws CommandLineException
    {
        String repositoryRoot = repositoryRootFile.getAbsolutePath();

        // TODO: it'd be great to build this into CommandLineUtils somehow
        // TODO: some way without a custom cygwin sys property?
        if ( "true".equals( System.getProperty( "cygwin" ) ) )
        {
            Commandline cl = new Commandline();

            cl.setExecutable( "cygpath" );

            cl.createArgument().setValue( "--unix" );

            cl.createArgument().setValue( repositoryRoot );

            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

            int exitValue = CommandLineUtils.executeCommandLine( cl, stdout, null );

            if ( exitValue != 0 )
            {
                throw new CommandLineException( "Unable to convert cygwin path, exit code = " + exitValue );
            }

            repositoryRoot = stdout.getOutput().trim();
        }
        else if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
        {
            repositoryRoot = "/" + StringUtils.replace( repositoryRoot, "\\", "/" );
        }

        return "scm:git:file://" + repositoryRoot;
    }
}
