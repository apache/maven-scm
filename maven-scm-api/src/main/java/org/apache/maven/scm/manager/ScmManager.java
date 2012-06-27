package org.apache.maven.scm.manager;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
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
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public interface ScmManager
{
    String ROLE = ScmManager.class.getName();

    // ----------------------------------------------------------------------
    // Repository
    // ----------------------------------------------------------------------

    /**
     * Generate a SCMRepository from a SCM url.
     *
     * @param scmUrl the scm url
     * @return The scm repository
     * @throws ScmRepositoryException     if an error occurs in the scm repository construction
     * @throws NoSuchScmProviderException if the provider doesn't exist
     */
    ScmRepository makeScmRepository( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException;

    ScmRepository makeProviderScmRepository( String providerType, File path )
        throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException;

    /**
     * Validate a SCM URL.
     *
     * @param scmUrl the SCM URL to validate
     * @return <code>List</code> of <code>String</code> objects with the messages returned by the SCM provider
     */
    List<String> validateScmRepository( String scmUrl );

    ScmProvider getProviderByUrl( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException;

    /**
     * Returns the default provider registered for this providerType or a specific implementation if the
     * 'maven.scm.provider.providerType.implementation' system property is defined. For example:
     * maven.scm.provider.cvs.implementation=cvs_native
     *
     * @param providerType The provider type (cvs, svn...)
     * @return The scm provider
     * @throws NoSuchScmProviderException if the provider doesn't exist
     */
    ScmProvider getProviderByType( String providerType )
        throws NoSuchScmProviderException;

    ScmProvider getProviderByRepository( ScmRepository repository )
        throws NoSuchScmProviderException;

    /**
     * Set a provider to be used for a type of SCM. If there was already a designed provider for that type it will be
     * replaced.
     *
     * @param providerType the type of SCM, eg. <code>svn</code>, <code>cvs</code>
     * @param provider     the provider that will be used for that SCM type
     */
    void setScmProvider( String providerType, ScmProvider provider );

    /**
     * Set the provider implementation
     *
     * @param providerType           The provider type, eg. <code>cvs</code>
     * @param providerImplementation The provider implementation (the role-hint of the provider), eg. <code>cvs</code>,
     *                               <code>cvs_native</code>
     */
    void setScmProviderImplementation( String providerType, String providerImplementation );

    /**
     * Adds the given files to the source control system
     *
     * @param repository the source control system
     * @param fileSet    the files to be added
     * @return an {@link org.apache.maven.scm.command.add.AddScmResult} that contains the files that have been added
     * @throws org.apache.maven.scm.ScmException
     *
     */
    AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Adds the given files to the source control system
     *
     * @param repository the source control system
     * @param fileSet    the files to be added
     * @param message    a string that is a comment on the new added file
     * @return an {@link AddScmResult} that contains the files that have been added
     * @throws ScmException if any
     */
    AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException;

    /**
     * Branch (or label in some systems) will create a branch of the source file with a certain branch name
     *
     * @param repository the source control system
     * @param fileSet    the files to branch. Implementations can also give the changes from the
     *                   {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param branchName the branch name to apply to the files
     * @return
     * @throws ScmException if any
     */
    BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName )
        throws ScmException;

    /**
     * Branch (or label in some systems) will create a branch of the source file with a certain branch name
     *
     * @param repository the source control system
     * @param fileSet    the files to branch. Implementations can also give the changes from the
     *                   {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param branchName the branch name to apply to the files
     * @param message    the commit message used for the tag creation
     * @return
     * @throws ScmException if any
     */
    BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName, String message )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system in a certain period of time. This can be
     * adding, removing, updating, ... of files
     *
     * @param repository the source control system
     * @param fileSet    the files to know the changes about. Implementations can also give the changes from the
     *                   {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param startDate  the start date of the period
     * @param endDate    the end date of the period
     * @param numDays    the number days before the current time if startdate and enddate are null
     * @param branch     the branch/tag
     * @return The SCM result of the changelog command
     * @throws ScmException if any
     * @deprecated use {@link #changeLog(org.apache.maven.scm.command.changelog.ChangeLogScmRequest)} instead
     */
    @Deprecated
    ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                  int numDays, ScmBranch branch )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system in a certain period of time. This can be
     * adding, removing, updating, ... of files
     *
     * @param repository  the source control system
     * @param fileSet     the files to know the changes about. Implementations can also give the changes from the
     *                    {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param startDate   the start date of the period
     * @param endDate     the end date of the period
     * @param numDays     the number days before the current time if startdate and enddate are null
     * @param branch      the branch/tag
     * @param datePattern the date pattern use in changelog output returned by scm tool
     * @return The SCM result of the changelog command
     * @throws ScmException if any
     * @deprecated use {@link #changeLog(org.apache.maven.scm.command.changelog.ChangeLogScmRequest)} instead
     */
    @Deprecated
    ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                  int numDays, ScmBranch branch, String datePattern )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system in a certain period of time. This can be
     * adding, removing, updating, ... of files
     *
     * @param scmRequest request wrapping detailed parameters for the changelog command
     * @return The SCM result of the changelog command
     * @throws ScmException if any
     * @since 1.8
     */
    ChangeLogScmResult changeLog( ChangeLogScmRequest scmRequest )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system between two tags. This can be adding,
     * removing, updating, ... of files
     *
     * @param repository   the source control system
     * @param fileSet      the files to know the changes about. Implementations can also give the changes from the
     *                     {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param startVersion the start branch/tag/revision
     * @param endVersion   the end branch/tag/revision
     * @return The SCM result of the changelog command
     * @throws ScmException if any
     * @deprecated use {@link #changeLog(org.apache.maven.scm.command.changelog.ChangeLogScmRequest)} instead
     */
    @Deprecated
    ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                                  ScmVersion endVersion )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system between two tags. This can be adding,
     * removing, updating, ... of files
     *
     * @param repository    the source control system
     * @param fileSet       the files to know the changes about. Implementations can also give the changes from the
     *                      {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param startRevision the start revision
     * @param endRevision   the end revision
     * @param datePattern   the date pattern use in changelog output returned by scm tool
     * @return
     * @throws ScmException if any
     * @deprecated use {@link #changeLog(org.apache.maven.scm.command.changelog.ChangeLogScmRequest)} instead
     */
    @Deprecated
    ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startRevision,
                                  ScmVersion endRevision, String datePattern )
        throws ScmException;

    /**
     * Save the changes you have done into the repository. This will create a new version of the file or directory in
     * the repository.
     * <p/>
     * When the fileSet has no entries, the fileSet.getBaseDir() is recursively committed. When the fileSet has entries,
     * the commit is non-recursive and only the elements in the fileSet are committed.
     *
     * @param repository the source control system
     * @param fileSet    the files to check in (sometimes called commit)
     * @param message    a string that is a comment on the changes that where done
     * @return
     * @throws ScmException if any
     */
    CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException;

    /**
     * Save the changes you have done into the repository. This will create a new version of the file or directory in
     * the repository.
     * <p/>
     * When the fileSet has no entries, the fileSet.getBaseDir() is recursively committed. When the fileSet has entries,
     * the commit is non-recursive and only the elements in the fileSet are committed.
     *
     * @param repository the source control system
     * @param fileSet    the files to check in (sometimes called commit)
     * @param revision   branch/tag/revision
     * @param message    a string that is a comment on the changes that where done
     * @return
     * @throws ScmException if any
     */
    CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, ScmVersion revision, String message )
        throws ScmException;

    /**
     * Create a copy of the repository on your local machine
     *
     * @param repository the source control system
     * @param fileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @return
     * @throws ScmException if any
     */
    CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Create a copy of the repository on your local machine
     *
     * @param repository the source control system
     * @param fileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param version    get the version defined by the revision, branch or tag
     * @return
     * @throws ScmException if any
     */
    CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException;

    /**
     * Create a copy of the repository on your local machine.
     *
     * @param scmRepository the source control system
     * @param scmFileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param recursive     whether to check out recursively
     * @return
     * @throws ScmException if any
     */
    CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, boolean recursive )
        throws ScmException;

    /**
     * Create a copy of the repository on your local machine.
     *
     * @param scmRepository the source control system
     * @param scmFileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param version       get the version defined by the revision, branch or tag
     * @param recursive     whether to check out recursively
     * @return
     * @throws ScmException if any
     */
    CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion version,
                                boolean recursive )
        throws ScmException;

    /**
     * Create a diff between two branch/tag/revision.
     *
     * @param scmRepository the source control system
     * @param scmFileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param startVersion  the start branch/tag/revision
     * @param endVersion    the end branch/tag/revision
     * @return
     * @throws ScmException if any
     */
    DiffScmResult diff( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion startVersion,
                        ScmVersion endVersion )
        throws ScmException;

    /**
     * Make a file editable. This is used in source control systems where you look at read-only files and you need to
     * make them not read-only anymore before you can edit them. This can also mean that no other user in the system can
     * make the file not read-only anymore.
     *
     * @param repository the source control system
     * @param fileSet    the files to make editable
     * @return
     * @throws ScmException if any
     */
    EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Create an exported copy of the repository on your local machine
     *
     * @param repository the source control system
     * @param fileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @return
     * @throws ScmException if any
     */
    ExportScmResult export( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Create an exported copy of the repository on your local machine
     *
     * @param repository the source control system
     * @param fileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param version    get the version defined by the branch/tag/revision
     * @return
     * @throws ScmException if any
     */
    ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException;

    /**
     * Create an exported copy of the repository on your local machine
     *
     * @param repository      the source control system
     * @param fileSet         the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param outputDirectory the directory where the export will be stored
     * @return
     * @throws ScmException if any
     */
    ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String outputDirectory )
        throws ScmException;

    /**
     * Create an exported copy of the repository on your local machine
     *
     * @param repository      the source control system
     * @param fileSet         the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param version         get the version defined by the branch/tag/revision
     * @param outputDirectory the directory where the export will be stored
     * @return
     * @throws ScmException if any
     */
    ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, String outputDirectory )
        throws ScmException;

    /**
     * List each element (files and directories) of <B>fileSet</B> as they exist in the repository.
     *
     * @param repository the source control system
     * @param fileSet    the files to list
     * @param recursive  descend recursively
     * @param version    use the version defined by the branch/tag/revision
     * @return the list of files in the repository
     */
    ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, ScmVersion version )
        throws ScmException;

    /**
     * Create new directory/directories in the repository.
     *
     * @param repository
     * @param fileSet
     * @param message
     * @param createInLocal
     * @return
     * @throws ScmException
     */
    MkdirScmResult mkdir( ScmRepository repository, ScmFileSet fileSet, String message, boolean createInLocal )
        throws ScmException;

    /**
     * Removes the given files from the source control system
     *
     * @param repository the source control system
     * @param fileSet    the files to be removed
     * @param message
     * @return
     * @throws ScmException if any
     */
    RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException;

    /**
     * Returns the status of the files in the source control system. The state of each file can be one of the
     * {@link org.apache.maven.scm.ScmFileStatus} flags.
     *
     * @param repository the source control system
     * @param fileSet    the files to know the status about. Implementations can also give the changes from the
     *                   {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @return
     * @throws ScmException if any
     */
    StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Tag (or label in some systems) will tag the source file with a certain tag
     *
     * @param repository the source control system
     * @param fileSet    the files to tag. Implementations can also give the changes from the
     *                   {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param tagName    the tag name to apply to the files
     * @return
     * @throws ScmException if any
     */
    TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName )
        throws ScmException;

    /**
     * Tag (or label in some systems) will tag the source file with a certain tag
     *
     * @param repository the source control system
     * @param fileSet    the files to tag. Implementations can also give the changes from the
     *                   {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param tagName    the tag name to apply to the files
     * @param message    the commit message used for the tag creation
     * @return
     * @throws ScmException if any
     */
    TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName, String message )
        throws ScmException;

    /**
     * Make a file no longer editable. This is the conterpart of
     * {@link #edit(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet)}. It makes the file
     * read-only again.
     *
     * @param repository the source control system
     * @param fileSet    the files to make uneditable
     * @return
     * @throws ScmException if any
     */
    UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @param version    use the version defined by the branch/tag/revision
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository   the source control system
     * @param fileSet      location of your local copy
     * @param runChangelog Run the changelog command after the update
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, boolean runChangelog )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository   the source control system
     * @param fileSet      location of your local copy
     * @param version      use the version defined by the branch/tag/revision
     * @param runChangelog Run the changelog command after the update
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, boolean runChangelog )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository  the source control system
     * @param fileSet     location of your local copy
     * @param datePattern the date pattern use in changelog output returned by scm tool
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String datePattern )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository  the source control system
     * @param fileSet     location of your local copy
     * @param version     use the version defined by the branch/tag/revision
     * @param datePattern the date pattern use in changelog output returned by scm tool
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, String datePattern )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @param lastUpdate
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, Date lastUpdate )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @param version    use the version defined by the branch/tag/revision
     * @param lastUpdate
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository  the source control system
     * @param fileSet     location of your local copy
     * @param lastUpdate  Date of last update
     * @param datePattern the date pattern use in changelog output returned by scm tool
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, Date lastUpdate, String datePattern )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository  the source control system
     * @param fileSet     location of your local copy
     * @param version     use the version defined by the branch/tag/revision
     * @param lastUpdate  Date of last update
     * @param datePattern the date pattern use in changelog output returned by scm tool
     * @return
     * @throws ScmException if any
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate,
                            String datePattern )
        throws ScmException;

    /**
     * Returns the blame of specified file
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @param filename   file
     * @return blame for specified file
     * @throws ScmException if any
     * @since 1.4
     */
    BlameScmResult blame( ScmRepository repository, ScmFileSet fileSet, String filename )
        throws ScmException;

    /**
     * @param blameScmRequest
     * @return blame for specified file
     * @throws ScmException if any
     * @since 1.4
     */
    BlameScmResult blame( BlameScmRequest blameScmRequest )
        throws ScmException;
}
