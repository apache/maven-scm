package org.apache.maven.scm.provider.clearcase;

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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.blame.ClearCaseBlameCommand;
import org.apache.maven.scm.provider.clearcase.command.changelog.ClearCaseChangeLogCommand;
import org.apache.maven.scm.provider.clearcase.command.checkin.ClearCaseCheckInCommand;
import org.apache.maven.scm.provider.clearcase.command.checkout.ClearCaseCheckOutCommand;
import org.apache.maven.scm.provider.clearcase.command.edit.ClearCaseEditCommand;
import org.apache.maven.scm.provider.clearcase.command.status.ClearCaseStatusCommand;
import org.apache.maven.scm.provider.clearcase.command.tag.ClearCaseTagCommand;
import org.apache.maven.scm.provider.clearcase.command.update.ClearCaseUpdateCommand;
import org.apache.maven.scm.provider.clearcase.repository.ClearCaseScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.util.ClearCaseUtil;
import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="clearcase"
 */
public class ClearCaseScmProvider
    extends AbstractScmProvider
{
    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    /**
     * Contains parameters loaded from clearcase-settings.xml
     */
    private Settings settings;

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        settings = ClearCaseUtil.getSettings();
        return new ClearCaseScmProviderRepository( getLogger(), scmSpecificUrl, settings );
    }

    /** {@inheritDoc} */
    public String getScmType()
    {
        return "clearcase";
    }

    /** {@inheritDoc} */
    public boolean requiresEditMode()
    {
        return true;
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        ClearCaseChangeLogCommand command = new ClearCaseChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        ClearCaseCheckInCommand command = new ClearCaseCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        ClearCaseCheckOutCommand command = new ClearCaseCheckOutCommand();

        command.setLogger( getLogger() );
        command.setSettings( settings );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        ClearCaseUpdateCommand command = new ClearCaseUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        ClearCaseTagCommand command = new ClearCaseTagCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        ClearCaseStatusCommand command = new ClearCaseStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet,
                                  CommandParameters parameters )
        throws ScmException
    {
        ClearCaseEditCommand command = new ClearCaseEditCommand();

        command.setLogger( getLogger() );

        return (EditScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        ClearCaseBlameCommand command = new ClearCaseBlameCommand();

        command.setLogger( getLogger() );

        return (BlameScmResult) command.execute( repository, fileSet, parameters );
    }
}
