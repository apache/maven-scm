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
import org.apache.maven.scm.provider.ScmProviderStub;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.ScmRepositoryStub;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Stub implementation of ScmManager for unit testing purposes. 
 * It allows setting the expected results that the different methods will return.
 * More information about Stubs on <a href="http://martinfowler.com/bliki/TestDouble.html">Martin Fowler's TestDouble</a>
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ScmManagerStub
    implements ScmManager
{

    private ScmRepository scmRepository;

    private ScmProvider scmProvider;

    private List<String> messages;

    /**
     * Creates a new stub with stub repository and provider, and empty list of messages
     */
    public ScmManagerStub()
    {
        setScmRepository( new ScmRepositoryStub() );
        setScmProvider( new ScmProviderStub() );
        setMessages( new ArrayList<String>( 0 ) );
    }

    public void setScmProvider( ScmProvider scmProvider )
    {
        this.scmProvider = scmProvider;
    }

    public ScmProvider getScmProvider()
    {
        return scmProvider;
    }

    /** {@inheritDoc} */
    public void setScmProvider( String providerType, ScmProvider provider )
    {
        setScmProvider( provider );
    }

    /** {@inheritDoc} */
    public void setScmProviderImplementation( String providerType, String providerImplementation )
    {
        //Do nothing there
    }

    public void setScmRepository( ScmRepository scmRepository )
    {
        this.scmRepository = scmRepository;
    }

    public ScmRepository getScmRepository()
    {
        return scmRepository;
    }

    /**
     * Set the messages to return in validateScmRepository
     *
     * @param messages <code>List</code> of <code>String</code> objects
     */
    public void setMessages( List<String> messages )
    {
        this.messages = messages;
    }

    /**
     * Get the messages to return in validateScmRepository
     *
     * @return <code>List</code> of <code>String</code> objects
     */
    public List<String> getMessages()
    {
        return messages;
    }

    /** {@inheritDoc} */
    public ScmRepository makeScmRepository( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        return getScmRepository();
    }

    /** {@inheritDoc} */
    public ScmRepository makeProviderScmRepository( String providerType, File path )
        throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException
    {
        return getScmRepository();
    }

    /**
     * Returns the same list as getMessages()
     *
     * @param scmUrl ignored
     * @return <code>List</code> of <code>String</code> objects, the same list returned by getMessages()
     */
    public List<String> validateScmRepository( String scmUrl )
    {
        return getMessages();
    }

    /** {@inheritDoc} */
    public ScmProvider getProviderByUrl( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        return getScmProvider();
    }

    /** {@inheritDoc} */
    public ScmProvider getProviderByType( String providerType )
        throws NoSuchScmProviderException
    {
        return getScmProvider();
    }

    /** {@inheritDoc} */
    public ScmProvider getProviderByRepository( ScmRepository repository )
        throws NoSuchScmProviderException
    {
        return getScmProvider();
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).add( repository, fileSet );
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).add( repository, fileSet, message );
    }

    /** {@inheritDoc} */
    @SuppressWarnings( "deprecation" )
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).branch( repository, fileSet, branchName );
    }

    /** {@inheritDoc} */
    @SuppressWarnings( "deprecation" )
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).branch( repository, fileSet, branchName, message );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startDate, endDate, numDays,
                                                                     branch );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch, String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startDate, endDate, numDays,
                                                                     branch, datePattern );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ChangeLogScmRequest request )
        throws ScmException
    {
        final ScmRepository repository = request.getScmRepository();
        return this.getProviderByRepository( repository ).changeLog( request );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                                         ScmVersion endVersion )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startVersion, endVersion );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startRevision,
                                         ScmVersion endRevision, String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startRevision, endRevision,
                                                                     datePattern );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkIn( repository, fileSet, message );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, ScmVersion revision, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkIn( repository, fileSet, revision, message );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkOut( repository, fileSet );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkOut( repository, fileSet, version );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, boolean recursive )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkOut( repository, fileSet, recursive );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                       boolean recursive )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkOut( repository, fileSet, version, recursive );
    }

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                               ScmVersion endVersion )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).diff( repository, fileSet, startVersion, endVersion );
    }

    /** {@inheritDoc} */
    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).edit( repository, fileSet );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).export( repository, fileSet );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).export( repository, fileSet, version );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String outputDirectory )
        throws ScmException
    {
        return this.export( repository, fileSet, outputDirectory );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String outputDirectory )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).export( repository, fileSet, version, outputDirectory );
    }

    /** {@inheritDoc} */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).list( repository, fileSet, recursive, version );
    }

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).remove( repository, fileSet, message );
    }

    /** {@inheritDoc} */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).status( repository, fileSet );
    }

    /** {@inheritDoc} */
    @SuppressWarnings( "deprecation" )
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).tag( repository, fileSet, tagName );
    }

    /** {@inheritDoc} */
    @SuppressWarnings( "deprecation" )
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).tag( repository, fileSet, tagName, message );
    }

    /** {@inheritDoc} */
    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).unedit( repository, fileSet );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, boolean runChangelog )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, runChangelog );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   boolean runChangelog )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, runChangelog );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, (ScmVersion) null, datePattern );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, datePattern );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, Date lastUpdate )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, (ScmVersion) null, lastUpdate );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, lastUpdate );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, Date lastUpdate, String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, (ScmVersion) null, lastUpdate,
                                                                  datePattern );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, lastUpdate,
                                                                  datePattern );
    }

    /** {@inheritDoc} */
    public BlameScmResult blame( ScmRepository repository, ScmFileSet fileSet, String filename )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).blame( repository, fileSet, filename );
    }

    /** {@inheritDoc} */
    public MkdirScmResult mkdir( ScmRepository repository, ScmFileSet fileSet, String message, boolean createInLocal )
        throws ScmException
    {  
        return this.getProviderByRepository( repository ).mkdir( repository, fileSet, message, createInLocal );
    }
}
