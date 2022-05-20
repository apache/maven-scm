package org.apache.maven.scm.provider;

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
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Stub implementation of ScmProvider for unit testing purposes.
 * It allows setting the expected results that the different methods will return.
 * More information about Stubs on
 * <a href="http://martinfowler.com/bliki/TestDouble.html">Martin Fowler's TestDouble</a>
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 *
 */
public class ScmProviderStub
    implements ScmProvider
{

    private String scmType, scmSpecificFilename;

    private boolean requiresEditmode;

    private ScmProviderRepository scmProviderRepository = new ScmProviderRepositoryStub();

    private List<String> errors = new ArrayList<String>();

    private AddScmResult addScmResult;

    private BranchScmResult branchScmResult;

    private CheckInScmResult checkInScmResult;

    private CheckOutScmResult checkOutScmResult;

    private ChangeLogScmResult changeLogScmResult;

    private DiffScmResult diffScmResult;

    private RemoveScmResult removeScmResult;

    private StatusScmResult statusScmResult;

    private TagScmResult tagScmResult;

    private UpdateScmResult updateScmResult;

    private EditScmResult editScmResult;

    private UnEditScmResult unEditScmResult;

    private ListScmResult listScmResult;

    private ExportScmResult exportScmResult;

    private BlameScmResult blameScmResult;

    private MkdirScmResult mkdirScmResult;

    private UntagScmResult untagScmResult;

    /**
     * Create a new ScmProviderStub with bogus (not null) attributes
     */
    public ScmProviderStub()
    {
        setScmSpecificFilename( "" );
        setAddScmResult( new AddScmResult( "", Collections.<ScmFile>emptyList() ) );
        setBranchScmResult( new BranchScmResult( "", Collections.<ScmFile>emptyList() ) );
        setChangeLogScmResult( new ChangeLogScmResult( "", "", "", true ) );
        setCheckInScmResult( new CheckInScmResult( "", "", "", true ) );
        setCheckOutScmResult( new CheckOutScmResult( "", "", "", true ) );
        setDiffScmResult( new DiffScmResult( "", "", "", true ) );
        setEditScmResult( new EditScmResult( "", "", "", true ) );
        setExportScmResult( new ExportScmResult( "", "", "", true ) );
        setRemoveScmResult( new RemoveScmResult( "", "", "", true ) );
        setStatusScmResult( new StatusScmResult( "", "", "", true ) );
        setTagScmResult( new TagScmResult( "", "", "", true ) );
        setUnEditScmResult( new UnEditScmResult( "", "", "", true ) );
        setUntagScmResult( new UntagScmResult( "", "", "", true ) );
        setUpdateScmResult( new UpdateScmResult( "", "", "", true ) );
        setBlameScmResult( new BlameScmResult( "", "", "", true ) );
        setMkdirScmResult( new MkdirScmResult( "", "", "", true ) );
    }

    /**
     * {@inheritDoc}
     */
    public String sanitizeTagName( String tag )
    {
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    public boolean validateTagName( String tag )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getScmType()
    {
        return scmType;
    }

    public void setScmSpecificFilename( String scmSpecificFilename )
    {
        this.scmSpecificFilename = scmSpecificFilename;
    }

    public boolean requiresEditMode()
    {
        return requiresEditmode;
    }

    public void setAddScmResult( AddScmResult addScmResult )
    {
        this.addScmResult = addScmResult;
    }

    public AddScmResult getAddScmResult()
    {
        return addScmResult;
    }

    public void setBranchScmResult( BranchScmResult branchScmResult )
    {
        this.branchScmResult = branchScmResult;
    }

    public BranchScmResult getBranchScmResult()
    {
        return branchScmResult;
    }

    public void setCheckInScmResult( CheckInScmResult checkInScmResult )
    {
        this.checkInScmResult = checkInScmResult;
    }

    public CheckInScmResult getCheckInScmResult()
    {
        return checkInScmResult;
    }

    public void setCheckOutScmResult( CheckOutScmResult checkOutScmResult )
    {
        this.checkOutScmResult = checkOutScmResult;
    }

    public CheckOutScmResult getCheckOutScmResult()
    {
        return checkOutScmResult;
    }

    public void setChangeLogScmResult( ChangeLogScmResult changeLogScmResult )
    {
        this.changeLogScmResult = changeLogScmResult;
    }

    public ChangeLogScmResult getChangeLogScmResult()
    {
        return changeLogScmResult;
    }

    public void setDiffScmResult( DiffScmResult diffScmResult )
    {
        this.diffScmResult = diffScmResult;
    }

    public DiffScmResult getDiffScmResult()
    {
        return diffScmResult;
    }

    public ExportScmResult getExportScmResult()
    {
        return exportScmResult;
    }

    public void setExportScmResult( ExportScmResult exportScmResult )
    {
        this.exportScmResult = exportScmResult;
    }

    public void setTagScmResult( TagScmResult tagScmResult )
    {
        this.tagScmResult = tagScmResult;
    }

    public TagScmResult getTagScmResult()
    {
        return tagScmResult;
    }

    public void setUntagScmResult( UntagScmResult untagScmResult )
    {
        this.untagScmResult = untagScmResult;
    }

    public UntagScmResult getUntagScmResult()
    {
        return untagScmResult;
    }

    public void setRemoveScmResult( RemoveScmResult removeScmResult )
    {
        this.removeScmResult = removeScmResult;
    }

    public RemoveScmResult getRemoveScmResult()
    {
        return removeScmResult;
    }

    public void setStatusScmResult( StatusScmResult statusScmResult )
    {
        this.statusScmResult = statusScmResult;
    }

    public StatusScmResult getStatusScmResult()
    {
        return statusScmResult;
    }

    public void setUpdateScmResult( UpdateScmResult updateScmResult )
    {
        this.updateScmResult = updateScmResult;
    }

    public UpdateScmResult getUpdateScmResult()
    {
        return updateScmResult;
    }

    public void setEditScmResult( EditScmResult editScmResult )
    {
        this.editScmResult = editScmResult;
    }

    public EditScmResult getEditScmResult()
    {
        return editScmResult;
    }

    public void setUnEditScmResult( UnEditScmResult unEditScmResult )
    {
        this.unEditScmResult = unEditScmResult;
    }

    public UnEditScmResult getUnEditScmResult()
    {
        return unEditScmResult;
    }

    public void setListScmResult( ListScmResult listScmResult )
    {
        this.listScmResult = listScmResult;
    }

    public ListScmResult getListScmResult()
    {
        return listScmResult;
    }

    public void setBlameScmResult( BlameScmResult blameScmResult )
    {
        this.blameScmResult = blameScmResult;
    }

    public BlameScmResult getBlameScmResult()
    {
        return blameScmResult;
    }

    public MkdirScmResult getMkdirScmResult()
    {
        return mkdirScmResult;
    }

    public void setMkdirScmResult( MkdirScmResult mkdirScmResult )
    {
        this.mkdirScmResult = mkdirScmResult;
    }

    /**
     * {@inheritDoc}
     */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        return scmProviderRepository;
    }

    /**
     * {@inheritDoc}
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        return scmProviderRepository;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        return errors;
    }

    /**
     * {@inheritDoc}
     */
    public String getScmSpecificFilename()
    {
        return scmSpecificFilename;
    }

    /**
     * {@inheritDoc}
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getAddScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return getAddScmResult();
    }

    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters commandParameters )
        throws ScmException
    {
        return getAddScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName )
        throws ScmException
    {
        return getBranchScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName, String message )
        throws ScmException
    {
        return getBranchScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName,
                                   ScmBranchParameters scmBranchParameters )
        throws ScmException
    {
        return getBranchScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch, String datePattern )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag,
                                         String datePattern )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch, String datePattern )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    public ChangeLogScmResult changeLog( ChangeLogScmRequest scmRequest )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                                         ScmVersion endVersion )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startRevision,
                                         ScmVersion endRevision, String datePattern )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        return getCheckInScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return getCheckInScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, ScmVersion revision, String message )
        throws ScmException
    {
        return getCheckInScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, String tag,
                                       boolean recursive )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, boolean recursive )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion version,
                                       boolean recursive )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    @Override
    public CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion version,
                                       CommandParameters commandParameters )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException
    {
        return getDiffScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public DiffScmResult diff( ScmRepository scmRepository, ScmFileSet scmFileSet, ScmVersion startVersion,
                               ScmVersion endVersion )
        throws ScmException
    {
        return getDiffScmResult();
    }

    /**
     * @return getUpdateScmResult() always
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate,
                                   String datePattern, boolean runChangelog )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getEditScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String tag, String outputDirectory )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String outputDirectory )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, String tag )
        throws ScmException
    {
        return getListScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, ScmVersion version )
        throws ScmException
    {
        return getListScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return getRemoveScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getStatusScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getTagScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        return getTagScmResult();
    }

    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName,
                             ScmTagParameters scmTagParameters )
        throws ScmException
    {
        return getTagScmResult();
    }

    public UntagScmResult untag( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return getUntagScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, boolean runChangelog )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, String datePattern )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, boolean runChangelog )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   boolean runChangelog )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String datePattern )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getUnEditScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public BlameScmResult blame( ScmRepository repository, ScmFileSet fileSet, String filename )
        throws ScmException
    {
        return getBlameScmResult();
    }

    public BlameScmResult blame( BlameScmRequest blameScmRequest )
        throws ScmException
    {
        return getBlameScmResult();
    }

    /**
     * {@inheritDoc}
     */
    public MkdirScmResult mkdir( ScmRepository repository, ScmFileSet fileSet, String message, boolean createInLocal )
        throws ScmException
    {
        return getMkdirScmResult();
    }

    public InfoScmResult info( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return new InfoScmResult( "", "", "", true );
    }

    public RemoteInfoScmResult remoteInfo( ScmProviderRepository repository, ScmFileSet fileSet,
                                           CommandParameters parameters )
        throws ScmException
    {
        return new RemoteInfoScmResult( "", null, null );
    }
}
