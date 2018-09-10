package org.apache.maven.scm.provider.git.gitexe.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class GitCheckInCommandTest
    extends ScmTestCase
{
    private File messageFile;

    private String messageFileString;

    public void setUp()
        throws Exception
    {
        super.setUp();

        messageFile = new File( "commit-message" );

        String path = messageFile.getAbsolutePath();
        if ( path.indexOf( ' ' ) >= 0 )
        {
            path = "\"" + path + "\"";
        }
        messageFileString = "-F " + path;
    }

    public void testCommandLineWithoutTag()
        throws Exception
    {
        if ( GitUtil.getSettings().isCommitNoVerify() )
        {
            testCommandLine( "scm:git:http://foo.com/git/trunk", "git commit --verbose " + messageFileString + " -a" + " --no-verify" );
        }
        else
        {
            testCommandLine( "scm:git:http://foo.com/git/trunk", "git commit --verbose " + messageFileString + " -a" );
        }
    }

    public void testCommandLineWithUsername()
        throws Exception
    {
        if ( GitUtil.getSettings().isCommitNoVerify() )
        {
            testCommandLine( "scm:git:http://anonymous@foo.com/git/trunk", "git commit --verbose " + messageFileString
                + " -a" + " --no-verify" );
        }
        else
        {
            testCommandLine( "scm:git:http://anonymous@foo.com/git/trunk", "git commit --verbose " + messageFileString
                + " -a" );
        }
    }

    // Test reproducing SCM-694
    public void testCheckinAfterRename() throws Exception {
        File repo = getRepositoryRoot();
        File checkedOutRepo = getWorkingCopy();

        if ( !ScmTestCase.isSystemCmd( "git" ) )
        {
            ScmTestCase.printSystemCmdUnavail( "git", getName() );
            return;
        }

        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());

        ScmRepository scmRepository = getScmManager().makeScmRepository(
            "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString() );
        checkoutRepoInto(checkedOutRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaultUser( checkedOutRepo );

        // Creating foo/bar/wine.xml
        File fooDir = new File( checkedOutRepo.getAbsolutePath(), "foo" );
        fooDir.mkdir();
        File barDir = new File(fooDir.getAbsolutePath(), "bar");
        barDir.mkdir();
        File wineFile = new File(barDir.getAbsolutePath(), "wine.xml");
        FileUtils.fileWrite( wineFile.getAbsolutePath(), "Lacoste castle" );

        // Adding and commiting file
        AddScmResult addResult = getScmManager().add( scmRepository, new ScmFileSet( checkedOutRepo, new File( "foo/bar/wine.xml" ) ) );
        assertResultIsSuccess( addResult );
        CheckInScmResult checkInScmResult = getScmManager().checkIn(scmRepository, new ScmFileSet(checkedOutRepo), "Created wine file");
        assertResultIsSuccess( checkInScmResult );

        // Cloning foo/bar/wine.xml to foo/newbar/wine.xml
        File newBarDir = new File(fooDir.getAbsolutePath(), "newbar");
        newBarDir.mkdir();
        File movedWineFile = new File(newBarDir.getAbsolutePath(), "wine.xml");
        FileUtils.copyFile(wineFile, movedWineFile);

        // Removing old file, adding new file and commiting...
        RemoveScmResult removeResult = getScmManager().remove(scmRepository, new ScmFileSet(checkedOutRepo, new File("foo/bar/")), "");
        assertResultIsSuccess(removeResult);
        addResult = getScmManager().add(scmRepository, new ScmFileSet(checkedOutRepo, new File("foo/newbar/wine.xml")));
        assertResultIsSuccess(addResult);
        checkInScmResult = getScmManager().checkIn(scmRepository, new ScmFileSet(checkedOutRepo), "moved wine.xml from foo/bar/ to foo/newbar/");
        assertResultIsSuccess(checkInScmResult);
        assertTrue("Renamed file has not been commited!", checkInScmResult.getCheckedInFiles().size() != 0);
    }

    // Test FileSet in configuration
    public void testCheckinWithFileSet() throws Exception {
        File repo = getRepositoryRoot();
        File checkedOutRepo = getWorkingCopy();

        if ( !ScmTestCase.isSystemCmd( "git" ) )
        {
            ScmTestCase.printSystemCmdUnavail( "git", getName() );
            return;
        }

        GitScmTestUtils.initRepo( "src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory() );

        ScmRepository scmRepository = getScmManager().makeScmRepository(
            "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString() );
        checkoutRepoInto( checkedOutRepo, scmRepository );

        // Add a default user to the config
        GitScmTestUtils.setDefaultUser( checkedOutRepo );

        // Creating beer.xml and whiskey.xml
        File beerFile = new File( checkedOutRepo.getAbsolutePath(), "beer.xml" );
        FileUtils.fileWrite( beerFile.getAbsolutePath(), "1/2 litre" );
        File whiskeyFile = new File( checkedOutRepo.getAbsolutePath(), "whiskey.xml" );
        FileUtils.fileWrite( whiskeyFile.getAbsolutePath(), "700 ml" );

        // Adding and commiting beer and whiskey
        AddScmResult addResult = getScmManager().add( scmRepository, new ScmFileSet( checkedOutRepo, "beer.xml,whiskey.xml" ) );
        assertResultIsSuccess( addResult );
        CheckInScmResult checkInScmResult = getScmManager().checkIn( scmRepository,
            new ScmFileSet( checkedOutRepo, "beer.xml,whiskey.xml" ), "Created beer file" );
        assertResultIsSuccess( checkInScmResult );

        // Editing beer and commiting whiskey, should commit nothingi, but succeed
        FileUtils.fileWrite( beerFile.getAbsolutePath(), "1 litre" );

        addResult = getScmManager().add( scmRepository, new ScmFileSet( checkedOutRepo, "whiskey.xml" ) );
        assertResultIsSuccess( addResult );
        checkInScmResult = getScmManager().checkIn( scmRepository,
            new ScmFileSet( checkedOutRepo, "whiskey.xml" ), "Checking beer file");
        assertResultIsSuccess( checkInScmResult );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private CheckOutScmResult checkoutRepoInto( File workingCopy, ScmRepository scmRepository )
        throws Exception {
        FileUtils.deleteDirectory( workingCopy );
        workingCopy.mkdir();

        CheckOutScmResult result =
            getScmManager().checkOut( scmRepository, new ScmFileSet( workingCopy ), (ScmVersion) null );

        assertResultIsSuccess( result );
        return result;
    }

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/git-checkin-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl =
            GitCheckInCommand.createCommitCommandLine( gitRepository, new ScmFileSet( workingDirectory ), messageFile );

        assertCommandLine( commandLine, workingDirectory, cl );
    }
}
