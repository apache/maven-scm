package org.apache.maven.scm.provider;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface ScmProvider
{
    String ROLE = ScmProvider.class.getName();

    String getScmType();

    void addListener( ScmLogger logger );

    boolean requiresEditMode();

    ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException;

    ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure;

    List validateScmUrl( String scmSpecificUrl, char delimiter );

    /**
     * Adds the given files to the source control system
     *
     * @param repository the source control system
     * @param fileSet    the files to be added
     * @return an {@link AddScmResult} that contains the files that have been added
     * @throws ScmException
     */
    AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system in a certain period of time.
     * This can be adding, removing, updating, ... of files
     *
     * @param repository the source control system
     * @param fileSet    the files to know the changes about. Implementations can also give the changes
     *                   from the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param startDate  the start date of the period
     * @param endDate    the end date of the period
     * @param numDays
     * @param branch
     * @return
     * @throws ScmException
     */
    ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                  int numDays, String branch )
        throws ScmException;

    /**
     * Returns the changes that have happend in the source control system between two tags.
     * This can be adding, removing, updating, ... of files
     *
     * @param repository the source control system
     * @param fileSet    the files to know the changes about. Implementations can also give the changes
     *                   from the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param startTag   the start tag
     * @param endTag     the end tag
     * @return
     * @throws ScmException
     */
    ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag )
        throws ScmException;

    /**
     * Save the changes you have done into the repository. This will create a new version of the file or
     * directory in the repository.
     *
     * @param repository the source control system
     * @param fileSet    the files to check in (sometimes called commit)
     * @param tag
     * @param message    a string that is a comment on the changes that where done
     * @return
     * @throws ScmException
     */
    CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException;

    /**
     * Create a copy of the repository on your local machine
     *
     * @param repository the source control system
     * @param fileSet    the files are copied to the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} location
     * @param tag        get the version defined by the tag
     * @return
     * @throws ScmException
     */
    CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException;

    DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException;

    /**
     * Removes the given files from the source control system
     *
     * @param repository the source control system
     * @param fileSet    the files to be removed
     * @param message
     * @return
     * @throws ScmException
     */
    RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException;

    /**
     * Returns the status of the files in the source control system. The state of each file can be one
     * of the {@link org.apache.maven.scm.ScmFileStatus} flags.
     *
     * @param repository the source control system
     * @param fileSet    the files to know the status about. Implementations can also give the changes
     *                   from the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @return
     * @throws ScmException
     */
    StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Tag (or label in some systems) will tag the source file with a certain tag
     *
     * @param repository the source control system
     * @param fileSet    the files to tag. Implementations can also give the changes
     *                   from the {@link org.apache.maven.scm.ScmFileSet#getBasedir()} downwards.
     * @param tag        the tag to apply to the files
     * @return
     * @throws ScmException
     */
    TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @param tag        use the version defined by the tag
     * @return
     * @throws ScmException
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException;

    /**
     * Updates the copy on the local machine with the changes in the repository
     *
     * @param repository the source control system
     * @param fileSet    location of your local copy
     * @param tag        use the version defined by the tag
     * @param lastUpdate
     * @return
     * @throws ScmException
     */
    UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException;

    /**
     * Make a file editable. This is used in source control systems where you look at read-only files
     * and you need to make them not read-only anymore before you can edit them. This can also mean
     * that no other user in the system can make the file not read-only anymore.
     *
     * @param repository the source control system
     * @param fileSet    the files to make editable
     * @return
     * @throws ScmException
     */
    EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;

    /**
     * Make a file no longer editable. This is the conterpart of {@link #edit(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet)}.
     * It makes the file read-only again.
     *
     * @param repository the source control system
     * @param fileSet    the files to make uneditable
     * @return
     * @throws ScmException
     */
    UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException;
}
