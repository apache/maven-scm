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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Stub implementation of ScmProvider for unit testing purposes. It allows setting the expected results that the different methods will return.
 * More information about Stubs on <a href="http://martinfowler.com/bliki/TestDouble.html">Martin Fowler's TestDouble</a>
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ScmProviderStub
    implements ScmProvider
{

    private String scmType, scmSpecificFilename;

    private List loggers = new ArrayList();

    private boolean requiresEditmode;

    private ScmProviderRepository scmProviderRepository = new ScmProviderRepositoryStub();

    private List errors = new ArrayList();

    private AddScmResult addScmResult;

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

    /**
     * Create a new ScmProviderStub with bogus (not null) attributes
     */
    public ScmProviderStub()
    {
        setScmSpecificFilename( "" );
        setAddScmResult( new AddScmResult( "", Collections.EMPTY_LIST ) );
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
        setUpdateScmResult( new UpdateScmResult( "", "", "", true ) );
    }

    public String sanitizeTagName( String tag )
    {
        return tag;
    }

    public boolean validateTagName( String tag )
    {
        return true;
    }

    public String getScmType()
    {
        return scmType;
    }

    public void setScmSpecificFilename( String scmSpecificFilename )
    {
        this.scmSpecificFilename = scmSpecificFilename;
    }

    public void addListener( ScmLogger logger )
    {
        loggers.add( logger );
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

    /**
     * @return scmProviderRepository always
     */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        return scmProviderRepository;
    }

    /**
     * @return scmProviderRepository always
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        return scmProviderRepository;
    }

    /**
     * @return errors always
     */
    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        return errors;
    }

    /**
     * @return scmSpecificFilename
     */
    public String getScmSpecificFilename()
    {
        return scmSpecificFilename;
    }

    /**
     * @return getAddScmResult() always
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getAddScmResult();
    }

    /**
     * @return getAddScmResult() always
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return getAddScmResult();
    }

    /**
     * @return getChangeLogScmResult() always
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * @return getChangeLogScmResult() always
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch, String datePattern )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * @return getChangeLogScmResult() always
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * @return getChangeLogScmResult() always
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag,
                                         String datePattern )
        throws ScmException
    {
        return getChangeLogScmResult();
    }

    /**
     * @return getCheckInScmResult() always
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        return getCheckInScmResult();
    }

    public CheckOutScmResult checkOut( ScmRepository scmRepository, ScmFileSet scmFileSet, String tag,
                                       boolean recursive )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * @return getCheckOutScmResult() always
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getCheckOutScmResult();
    }

    /**
     * @return getDiffScmResult() always
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException
    {
        return getDiffScmResult();
    }

    /**
     * @return getExportScmResult() always
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * @return getExportScmResult() always
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String tag, String outputDirectory )
        throws ScmException
    {
        return getExportScmResult();
    }

    /**
     * @return getRemoveScmResult() always
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return getRemoveScmResult();
    }

    /**
     * @return getStatusScmResult() always
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getStatusScmResult();
    }

    /**
     * @return getTagScmResult() always
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getTagScmResult();
    }

    /**
     * @return getUpdateScmResult() always
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * @return getUpdateScmResult() always
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, boolean runChangelog )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * @return getUpdateScmResult() always
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, String datePattern )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * @return getUpdateScmResult() always
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException
    {
        return getUpdateScmResult();
    }

    /**
     * @return getUpdateScmResult() always
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        return getUpdateScmResult();
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
     * @return getEditScmResult() always
     */
    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getEditScmResult();
    }

    /**
     * @return getUnEditScmResult() always
     */
    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return getUnEditScmResult();
    }

    /**
     * @return {@link #getListScmResult()} always
     */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, String tag )
        throws ScmException
    {
        return getListScmResult();
    }
}
