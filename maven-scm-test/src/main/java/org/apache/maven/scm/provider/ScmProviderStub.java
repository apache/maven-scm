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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

/**
 * Stub for ScmProvider
 * 
 * @TODO make stub methods return objects provided by user instead of null
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ScmProviderStub
    implements ScmProvider
{

    private String scmType;

    private List loggers = new ArrayList();
    
    private boolean requiresEditmode;

    private ScmProviderRepository scmProviderRepository = new ScmProviderRepositoryStub();
    
    private List errors = new ArrayList();

    public String getScmType()
    {
        return scmType;
    }

    public void addListener( ScmLogger logger )
    {
        loggers.add( logger );
    }

    public boolean requiresEditMode()
    {
        return requiresEditmode;
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

    public String getScmSpecificFilename()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                        int numDays, String branch )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                        int numDays, String branch, String datePattern )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag,
                                        String datePattern )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, String datePattern )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate,
                                  String datePattern )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
