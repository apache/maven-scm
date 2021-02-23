package org.apache.maven.scm.provider.mountrepos;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmBranchParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmRequest;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.mountrepos.MountReposScmProviderRepository.MountProjectRepository;
import org.apache.maven.scm.providers.mountrepos.manifest.MountReposManifest;
import org.apache.maven.scm.providers.mountrepos.manifest.io.xpp3.MountReposManifestXpp3Reader;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="mount-repos"
 * @since 1.11
 */
public class MountReposScmProvider
    extends AbstractScmProvider
{

    public static final String REPO_DIRNAME = ".repo";

    /**
     * @plexus.requirement role="org.apache.maven.scm.manager.ScmManager"
     */
    private ScmManager scmManager;

    /**
     * @plexus.requirement role="org.apache.maven.scm.provider.ScmProvider" role-hint="jgit"
     */
    protected JGitScmProvider gitScmProvider;

    @Override
    public String getScmType()
    {
        return "mount-repos";
    }

    @Override
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        if ( scmSpecificUrl == null )
        {
            throw new ScmRepositoryException( "url must not be null" );
        }
        File repoManifestFile = new File( scmSpecificUrl );
        if ( !repoManifestFile.exists() )
        {
            throw new ScmRepositoryException( "manifest file not found '" + repoManifestFile.getAbsolutePath() + "'" );
        }
        MountReposManifest reposManifest = parseReposManifest( repoManifestFile );
        return new MountReposScmProviderRepository( repoManifestFile, reposManifest, scmManager );
    }

    @Override
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        File manifestFile = new File( path, REPO_DIRNAME + "/defaut.xml" );
        if ( !manifestFile.exists() )
        {
            throw new ScmRepositoryException( "manifest file not found '" + manifestFile.getAbsolutePath() + "'" );
        }
        MountReposManifest reposManifest = parseReposManifest( manifestFile );
        return new MountReposScmProviderRepository( manifestFile, reposManifest, scmManager );
    }

    private MountReposManifest parseReposManifest( File repoManifestFile )
        throws ScmRepositoryException
    {
        MountReposManifestXpp3Reader reader = new MountReposManifestXpp3Reader();
        Reader fileReader = null;
        try
        {
            fileReader = new InputStreamReader( new FileInputStream( repoManifestFile ) );
            return reader.read( fileReader, false );
        }
        catch ( IOException | XmlPullParserException ex )
        {
            throw new ScmRepositoryException( "Failed to parse repos manifest xml file '"
                + repoManifestFile.getAbsolutePath() + "'", ex );
        }
        finally
        {
            if ( fileReader != null )
            {
                try
                {
                    fileReader.close();
                }
                catch ( IOException ex )
                {
                    getLogger().warn( "Failed to close file ..ignore" );
                }
            }
        }
    }

    /**
     * Validate the scm url.
     *
     * @param scmSpecificUrl The SCM url
     * @param delimiter The delimiter used in the SCM url
     * @return Returns a list of messages if the validation failed
     */
    @Override
    public List<String> validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        return super.validateScmUrl( scmSpecificUrl, delimiter );
    }

    /**
     * Returns the scm reserved file name where the SCM stores information like 'CVS', '.svn'.
     *
     * @return the scm reserved file name
     */
    @Override
    public String getScmSpecificFilename()
    {
        return REPO_DIRNAME;
    }

    /**
     * Check if this tag is valid for this SCM provider.
     *
     * @param tag tag name to check
     * @return true if tag is valid
     */
    @Override
    public boolean validateTagName( String tag )
    {
        return gitScmProvider.validateTagName( tag );
    }

    /**
     * Given a tag name, make it suitable for this SCM provider. For example, CVS converts "." into "_"
     *
     * @param tag input tag name
     * @return sanitized tag name
     */
    @Override
    public String sanitizeTagName( String tag )
    {
        return gitScmProvider.sanitizeTagName( tag );
    }

    /**
     * Adds the given files to the source control system
     *
     * @param repository the source control system
     * @param fileSet the files to be added
     * @param commandParameters {@link CommandParameters}
     * @return an {@link AddScmResult} that contains the files that have been added
     * @throws ScmException if any
     */
    @Override
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters commandParameters )
        throws ScmException
    {
        // TODO
        return super.add( repository, fileSet, commandParameters );
    }

    /**
     * Branch (or label in some systems) will create a branch of the source file with a certain branch name
     *
     * @param repository the source control system
     * @param fileSet the files to branch. Implementations can also give the changes from the
     *            {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param branchName the branch name to apply to the files
     * @return
     * @throws ScmException if any
     * @since 1.3
     */
    @Override
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName,
                                   ScmBranchParameters scmBranchParameters )
        throws ScmException
    {
        // TODO
        return branch( repository, fileSet, branchName, scmBranchParameters );
    }

    /**
     * Returns the changes that have happend in the source control system in a certain period of time. This can be
     * adding, removing, updating, ... of files
     *
     * @param scmRequest request wrapping detailed parameters for the changelog command
     * @return The SCM result of the changelog command
     * @throws ScmException if any
     * @since 1.8
     */
    @Override
    public ChangeLogScmResult changeLog( ChangeLogScmRequest scmRequest )
        throws ScmException
    {
        // TODO
        return super.changeLog( scmRequest );
    }

    /**
     * Save the changes you have done into the repository. This will create a new version of the file or directory in
     * the repository.
     * <p/>
     * When the fileSet has no entries, the fileSet.getBaseDir() is recursively committed. When the fileSet has entries,
     * the commit is non-recursive and only the elements in the fileSet are committed.
     *
     * @param repository the source control system
     * @param fileSet the files to check in (sometimes called commit)
     * @param revision branch/tag/revision
     * @param message a string that is a comment on the changes that where done
     * @return
     * @throws ScmException if any
     */
    @Override
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, ScmVersion revision, String message )
        throws ScmException
    {
        // TODO
        return super.checkIn( repository, fileSet, revision, message );
    }

    /**
     * Create a copy of the repository on your local machine.
     *
     * @param scmRepository the source control system
     * @param scmFileSet the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param version get the version defined by the revision, branch or tag
     * @param parameters parameters
     * @return
     * @throws ScmException if any
     * @since 1.9.6
     */
    @Override
    public CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion scmVersion, //
                                       CommandParameters parameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) scmRepository.getProviderRepository();
        boolean recursive = parameters.getBoolean( CommandParameter.RECURSIVE, true );

        File checkoutBaseDir = scmFileSet.getBasedir();

        List<ScmFile> checkedOutFiles = new ArrayList<ScmFile>();
        for ( MountProjectRepository mountProject : mountRepos.getProjectScmProviderRepositories().values() )
        {
            ScmRepository mountScmRepository = mountProject.getScmRepository();
            ScmFileSet mountScmFileSet = new ScmFileSet( new File( checkoutBaseDir, mountProject.getPath() ) );

            CheckOutScmResult mountRes =
                scmManager.checkOut( mountScmRepository, mountScmFileSet, scmVersion, recursive );

            checkedOutFiles.addAll( mountRes.getCheckedOutFiles() );
        }

        copyManifestToRepoSpecificFile( mountRepos, checkoutBaseDir );

        String versionName = ( scmVersion != null ) ? scmVersion.getName() : null;
        return new CheckOutScmResult( "repo init -u " + mountRepos.getRepoManifestFile(), versionName,
                                      checkedOutFiles );
    }

    @Override
    protected CheckOutScmResult checkout( ScmProviderRepository scmRepository, ScmFileSet scmFileSet,
                                          CommandParameters parameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos = (MountReposScmProviderRepository) scmRepository;
        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );
        boolean recursive = parameters.getBoolean( CommandParameter.RECURSIVE, true );
        // boolean shallow = parameters.getBoolean( CommandParameter.SHALLOW, false );

        File checkoutBaseDir = scmFileSet.getBasedir();

        List<ScmFile> checkedOutFiles = new ArrayList<ScmFile>();
        for ( MountProjectRepository mountProject : mountRepos.getProjectScmProviderRepositories().values() )
        {
            ScmRepository mountScmRepository = mountProject.getScmRepository();
            ScmFileSet mountScmFileSet = new ScmFileSet( new File( checkoutBaseDir, mountProject.getPath() ) );

            CheckOutScmResult mountRes =
                scmManager.checkOut( mountScmRepository, mountScmFileSet, scmVersion, recursive );

            checkedOutFiles.addAll( mountRes.getCheckedOutFiles() );
        }

        copyManifestToRepoSpecificFile( mountRepos, checkoutBaseDir );

        String versionName = ( scmVersion != null ) ? scmVersion.getName() : null;
        return new CheckOutScmResult( "repo update", versionName, checkedOutFiles );
    }

    /**
     * @param mountRepos
     * @param checkoutBaseDir
     * @throws ScmException
     */
    private void copyManifestToRepoSpecificFile( MountReposScmProviderRepository mountRepos, File checkoutBaseDir )
        throws ScmException
    {
        File repoScmSpecificDir = new File( checkoutBaseDir, REPO_DIRNAME );
        if ( !repoScmSpecificDir.exists() )
        {
            repoScmSpecificDir.mkdirs();
        }
        File manifestFile = new File( checkoutBaseDir, REPO_DIRNAME + "/defaut.xml" );
        try
        {
            FileUtils.copyFile( mountRepos.getRepoManifestFile(), manifestFile );
        }
        catch ( IOException ex )
        {
            throw new ScmException( "Failed to copy file " + manifestFile, ex );
        }
    }

    /**
     * Create a diff between two branch/tag/revision.
     *
     * @param scmRepository the source control system
     * @param scmFileSet the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param startVersion the start branch/tag/revision
     * @param endVersion the end branch/tag/revision
     * @return
     * @throws ScmException if any
     */
    @Override
    public DiffScmResult diff( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion startVersion,
                               ScmVersion endVersion )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) scmRepository.getProviderRepository();
        // TODO
        return super.diff( scmRepository, scmFileSet, startVersion, endVersion );
    }

    /**
     * Create an exported copy of the repository on your local machine
     *
     * @param repository the source control system
     * @param fileSet the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param version get the version defined by the branch/tag/revision
     * @param outputDirectory the directory where the export will be stored
     * @return
     * @throws ScmException if any
     */
    @Override
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String outputDirectory )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.export( repository, fileSet, version, outputDirectory );
    }

    /**
     * Removes the given files from the source control system
     *
     * @param repository the source control system
     * @param fileSet the files to be removed
     * @param message
     * @return
     * @throws ScmException if any
     */
    @Override
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.remove( repository, fileSet, message );
    }

    /**
     * Returns the status of the files in the source control system. The state of each file can be one of the
     * {@link org.apache.maven.scm.ScmFileStatus} flags.
     *
     * @param repository the source control system
     * @param fileSet the files to know the status about. Implementations can also give the changes from the
     *            {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @return
     * @throws ScmException if any
     */
    @Override
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.status( repository, fileSet );
    }

    /**
     * Deletes a tag.
     *
     * @param repository the source control system
     * @param fileSet a fileset with the relevant working directory as basedir
     * @param parameters
     * @return
     * @throws ScmException if any
     */
    @Override
    public UntagScmResult untag( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.untag( repository, fileSet, parameters );
    }

    /**
     * Tag (or label in some systems) will tag the source file with a certain tag
     *
     * @param repository the source control system
     * @param fileSet the files to tag. Implementations can also give the changes from the
     *            {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param tagName the tag name to apply to the files
     * @param scmTagParameters bean to pass some paramters for tagging {@link ScmTagParameters}
     * @return
     * @throws ScmException if any
     * @since 1.2
     */
    @Override
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName,
                             ScmTagParameters scmTagParameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.tag( repository, fileSet, tagName, scmTagParameters );
    }

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet location of your local copy
     * @param version use the version defined by the branch/tag/revision
     * @param lastUpdate Date of last update
     * @param datePattern the date pattern use in changelog output returned by scm tool
     * @return
     * @throws ScmException if any
     */
    @Override
    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos = (MountReposScmProviderRepository) repository;
        File checkoutBaseDir = fileSet.getBasedir();

        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );
        String datePattern = parameters.getString( CommandParameter.CHANGELOG_DATE_PATTERN, null );
        // boolean runChangelog = parameters.getBoolean(
        // CommandParameter.RUN_CHANGELOG_WITH_UPDATE );

        List<ScmFile> updatedFiles = new ArrayList<ScmFile>();
        for ( MountProjectRepository mountProject : mountRepos.getProjectScmProviderRepositories().values() )
        {
            ScmRepository mountScmRepository = mountProject.getScmRepository();
            // ScmProviderRepository mountScmProviderRepository =
            // mountScmRepository.getProviderRepository();
            ScmFileSet mountScmFileSet = new ScmFileSet( new File( checkoutBaseDir, mountProject.getPath() ) );
            ScmVersion mountScmVersion =
                ( mountProject.revision != null ) ? new ScmBranch( mountProject.revision ) : null;
            getLogger().info( "checkout or update project:" + mountProject.getRepoProject().getName() + " path="
                + mountProject.getPath() + " branch=" + mountProject.revision );

            try
            {
                UpdateScmResult mountRes =
                    scmManager.update( mountScmRepository, mountScmFileSet, mountScmVersion, datePattern );

                updatedFiles.addAll( mountRes.getUpdatedFiles() );
            }
            catch ( UnsupportedOperationException ex )
            {
                CheckOutScmResult checkOutResult =
                    scmManager.checkOut( mountScmRepository, mountScmFileSet, mountScmVersion );
                // ?? updatedFiles.add();
            }
            getLogger().info( "" );
        }
        return new UpdateScmResult( "repo update", updatedFiles );
    }

    /**
     * Make a file editable. This is used in source control systems where you look at read-only files and you need to
     * make them not read-only anymore before you can edit them. This can also mean that no other user in the system can
     * make the file not read-only anymore.
     *
     * @param repository the source control system
     * @param fileSet the files to make editable
     * @return
     * @throws ScmException if any
     */
    @Override
    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.edit( repository, fileSet );
    }

    /**
     * Make a file no longer editable. This is the conterpart of
     * {@link #edit( org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet)}. It makes the file
     * read-only again.
     *
     * @param repository the source control system
     * @param fileSet the files to make uneditable
     * @return
     * @throws ScmException if any
     */
    @Override
    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.unedit( repository, fileSet );
    }

    /**
     * List each element (files and directories) of <B>fileSet</B> as they exist in the repository.
     *
     * @param repository the source control system
     * @param fileSet the files to list
     * @param recursive descend recursively
     * @param version use the version defined by the branch/tag/revision
     * @return the list of files in the repository
     * @throws ScmException if any
     */
    @Override
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, ScmVersion version )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.list( repository, fileSet, recursive, version );
    }

    /**
     * @param blameScmRequest
     * @return blame for the file specified in the request
     * @throws ScmException
     * @since 1.8
     */
    @Override
    public BlameScmResult blame( BlameScmRequest blameScmRequest )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) blameScmRequest.getScmRepository().getProviderRepository();
        // TODO
        return super.blame( blameScmRequest );
    }

    /**
     * Create directory/directories in the repository.
     *
     * @param repository
     * @param fileSet
     * @param createInLocal
     * @param message
     * @return
     * @throws ScmException
     */
    @Override
    public MkdirScmResult mkdir( ScmRepository repository, ScmFileSet fileSet, String message, boolean createInLocal )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos =
            (MountReposScmProviderRepository) repository.getProviderRepository();
        // TODO
        return super.mkdir( repository, fileSet, message, createInLocal );
    }

    /**
     * @param repository the source control system
     * @param fileSet location of your local copy
     * @param parameters some parameters (not use currently but for future use)
     * @return if the scm implementation doesn't support "info" result will <code>null</code>
     * @throws ScmException
     * @since 1.5
     */
    @Override
    public InfoScmResult info( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos = (MountReposScmProviderRepository) repository;
        // TODO
        return super.info( repository, fileSet, parameters );
    }

    /**
     * @param scmRepository the source control system
     * @param fileSet not use currently but for future use
     * @param parameters some parameters (not use currently but for future use)
     * @return if the scm implementation doesn't support "info" result will <code>null</code>
     * @throws ScmException
     * @since 1.6
     */
    @Override
    public RemoteInfoScmResult remoteInfo( ScmProviderRepository scmRepository, ScmFileSet fileSet,
                                           CommandParameters parameters )
        throws ScmException
    {
        MountReposScmProviderRepository mountRepos = (MountReposScmProviderRepository) scmRepository;
        // TODO
        return super.remoteInfo( scmRepository, fileSet, parameters );
    }

}
