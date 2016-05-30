package org.apache.maven.scm.provider.git.jgit.command.checkin;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.checkin.GitCheckInCommandTckTest;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.SystemReader;

/**
 * @author Dominik Bartholdi (imod)
 */
public class JGitCheckInCommandCommitterAuthorTckTest
    extends GitCheckInCommandTckTest
{
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        SystemReader.setInstance( new CustomSystemReader() );
    }
    
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        
        // back to default
        SystemReader.setInstance( null );
    }
    
    /**
     * {@inheritDoc}
     */
    public String getScmUrl()
        throws Exception
    {
        return GitScmTestUtils.getScmUrl( getRepositoryRoot(), "jgit" );
    }

    @Override
    protected void deleteDirectory( File directory )
        throws IOException
    {
        if ( directory.exists() )
        {
            FileUtils.delete( directory, FileUtils.RECURSIVE | FileUtils.RETRY );
        }
    }

    @Override
    public void testCheckInCommandTest()
        throws Exception
    {
        File fooJava = new File( getWorkingCopy(), "src/main/java/Foo.java" );
        assertFalse( "check Foo.java doesn't yet exist", fooJava.canRead() );

        Git git = Git.open( getWorkingCopy() );

        RevCommit head = getHeadCommit( git.getRepository() );
        // Mark created the test repo...
        assertEquals( "Mark Struberg", head.getCommitterIdent().getName() );
        JGitUtils.closeRepo( git );

        createAndCommitFile( fooJava, null );

        // change user in config
        git = Git.open( getWorkingCopy() );
        StoredConfig config = git.getRepository().getConfig();
        unsetConfig( config );
        config.setString( "user", null, "name", "Dominik" );
        config.setString( "user", null, "email", "domi@mycomp.com" );
        config.save();

        // make a commit
        createAndCommitFile( fooJava, null );

        // check new commit is done with new user in config
        head = getHeadCommit( git.getRepository() );
        assertEquals( "Dominik", head.getCommitterIdent().getName() );
        assertEquals( "Dominik", head.getAuthorIdent().getName() );
        assertEquals( "domi@mycomp.com", head.getAuthorIdent().getEmailAddress() );
        assertEquals( "domi@mycomp.com", head.getCommitterIdent().getEmailAddress() );
        JGitUtils.closeRepo( git );

        // change user in config
        git = Git.open( getWorkingCopy() );
        config = git.getRepository().getConfig();
        unsetConfig( config );
        config.setString( "user", null, "name", "dbartholdi" );
        config.save();

        // make a change
        createAndCommitFile( fooJava, null );

        // check new commit is done with new user in config
        head = getHeadCommit( git.getRepository() );
        assertEquals( "dbartholdi", head.getCommitterIdent().getName() );
        assertFalse( "no mail domain is configured, git system default should be used",
                     head.getCommitterIdent().getEmailAddress().contains( "dbartholdi" ) );
        JGitUtils.closeRepo( git );

        // unset a user and maven user but set default mail domain
        git = Git.open( getWorkingCopy() );
        config = git.getRepository().getConfig();
        unsetConfig( config );
        config.setString( JGitCheckInCommand.GIT_MAVEN_SECTION, null, JGitCheckInCommand.GIT_MAILDOMAIN, "comp.com" );
        config.save();

        // make a change with an user on the commandline
        createAndCommitFile( fooJava, "dude" );

        // check new commit is done with new maven user in config
        head = getHeadCommit( git.getRepository() );
        assertEquals( "dude", head.getCommitterIdent().getName() );
        assertEquals( "dude@comp.com", head.getCommitterIdent().getEmailAddress() );
        assertEquals( "dude", head.getAuthorIdent().getName() );
        assertEquals( "dude@comp.com", head.getAuthorIdent().getEmailAddress() );
        JGitUtils.closeRepo( git );

        // unset a user and maven user but set default mail domain
        git = Git.open( getWorkingCopy() );
        config = git.getRepository().getConfig();
        unsetConfig( config );
        config.setString( "user", null, "name", "dbartholdi" );
        config.setBoolean( JGitCheckInCommand.GIT_MAVEN_SECTION, null, JGitCheckInCommand.GIT_FORCE, true );
        config.setString( JGitCheckInCommand.GIT_MAVEN_SECTION, null, JGitCheckInCommand.GIT_MAILDOMAIN, "anycomp.com" );
        config.save();

        // make a change with an user on the commandline
        createAndCommitFile( fooJava, "dude" );

        // check new commit is done with new maven user in config
        head = getHeadCommit( git.getRepository() );
        assertEquals( "dude", head.getCommitterIdent().getName() );
        assertEquals( "dude@anycomp.com", head.getCommitterIdent().getEmailAddress() );
        assertEquals( "dude", head.getAuthorIdent().getName() );
        assertEquals( "dude@anycomp.com", head.getAuthorIdent().getEmailAddress() );
        JGitUtils.closeRepo( git );

        // unset a user and maven user but set default mail domain
        git = Git.open( getWorkingCopy() );
        config = git.getRepository().getConfig();
        unsetConfig( config );
        config.setString( JGitCheckInCommand.GIT_MAVEN_SECTION, null, JGitCheckInCommand.GIT_MAILDOMAIN, "anycomp.com" );
        config.save();

        // make a change with no username given
        createAndCommitFile( fooJava, null );

        // check new commit does not contain the configured email domain
        head = getHeadCommit( git.getRepository() );
        assertFalse( head.getCommitterIdent().getEmailAddress().contains( "anycomp.com" ) );
        assertFalse( head.getAuthorIdent().getEmailAddress().contains( "anycomp.com" ) );
        JGitUtils.closeRepo( git );

        // unset a user and full maven section
        git = Git.open( getWorkingCopy() );
        config = git.getRepository().getConfig();
        unsetConfig( config );
        config.save();

        // make a change with an user on the commandline
        createAndCommitFile( fooJava, "dundy" );

        // check new commit is done with new maven user in config
        head = getHeadCommit( git.getRepository() );
        assertEquals( "dundy", head.getCommitterIdent().getName() );
        assertEquals( "dundy", head.getAuthorIdent().getName() );
        assertTrue( "the maven user (from parameter) name must be in the committer mail when nothing else is configured",
                    head.getCommitterIdent().getEmailAddress().contains( "dundy" ) );
        assertTrue( "the user name (from parameter) must be in the author mail when nothing else is configured",
                    head.getAuthorIdent().getEmailAddress().contains( "dundy" ) );
        JGitUtils.closeRepo( git );

        // unset all configs
        git = Git.open( getWorkingCopy() );
        config = git.getRepository().getConfig();
        unsetConfig( config );
        config.save();

        // make a change with no user on the commandline
        createAndCommitFile( fooJava, null );

        // check new commit is has a committer/author with email set
        head = getHeadCommit( git.getRepository() );
        assertNotNull( head.getCommitterIdent().getName() );
        assertNotNull( head.getAuthorIdent().getName() );
        assertNotNull( head.getCommitterIdent().getEmailAddress() );
        assertNotNull( head.getAuthorIdent().getEmailAddress() );
        JGitUtils.closeRepo( git );
    }

    /**
     * make sure the local .gitconfig is in a clean state
     */
    private void unsetConfig( StoredConfig config )
    {
        config.unsetSection( "user", null );
        config.unset( "user", null, "name" );
        // somehow unset does not always work on "user"
        config.setString( "user", null, "name", null );
        config.setString( "user", null, "email", null );
        config.unsetSection( JGitCheckInCommand.GIT_MAVEN_SECTION, null );
    }

    private void createAndCommitFile( File file, String username )
        throws Exception, ScmException, IOException
    {
        createFooJava( file );

        ScmRepository scmRepository = getScmRepository();
        scmRepository.getProviderRepository().setUser( username );
        AddScmResult addResult = getScmManager().add( scmRepository, new ScmFileSet( getWorkingCopy(), "**/*.java" ) );

        assertResultIsSuccess( addResult );

        CheckInScmResult result =
            getScmManager().checkIn( scmRepository, new ScmFileSet( getWorkingCopy(), "**/Foo.java" ), "Commit message" );

        assertResultIsSuccess( result );
    }

    private RevCommit getHeadCommit( Repository repository )
        throws Exception
    {
        RevWalk rw = new RevWalk( repository );
        AnyObjectId headId = repository.resolve( Constants.HEAD );
        RevCommit head = rw.parseCommit( headId );
        rw.release();
        return head;
    }

    private void createFooJava( File fooJava )
        throws Exception
    {
        FileWriter output = new FileWriter( fooJava );

        PrintWriter printer = new PrintWriter( output );
        try
        {
            printer.println( "public class Foo" );
            printer.println( "{" );

            printer.println( "    public void foo()" );
            printer.println( "    {" );
            printer.println( "        //" + System.currentTimeMillis() );
            printer.println( "        int i = 10;" );
            printer.println( "    }" );

            printer.println( "}" );
        }
        finally
        {
            IOUtil.close( output );
            IOUtil.close( printer );
        }
    }

    /**
     * SystemReader for testing to have full control some imported getters
     * 
     * @author Robert Scholte
     */
    class CustomSystemReader extends SystemReader {
        
        private final SystemReader reader = SystemReader.getInstance();

        // Ensure environment properties from CI server don't get pulled in
        public String getenv( String variable )
        {
            return null;
        }

        @Override
        public String getHostname()
        {
            return reader.getHostname();
        }

        @Override
        public String getProperty( String key )
        {
            return reader.getProperty( key );
        }

        @Override
        public FileBasedConfig openSystemConfig( Config parent, FS fs )
        {
            return reader.openSystemConfig( parent, fs );
        }

        @Override
        public FileBasedConfig openUserConfig( Config parent, FS fs )
        {
            return reader.openUserConfig( parent, fs );
        }

        @Override
        public long getCurrentTime()
        {
            return reader.getCurrentTime();
        }

        @Override
        public int getTimezone( long when )
        {
            return reader.getTimezone( when );
        }
    }
    
}
