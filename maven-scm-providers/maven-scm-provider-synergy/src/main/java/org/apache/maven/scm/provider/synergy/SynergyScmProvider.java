package org.apache.maven.scm.provider.synergy;

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
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.add.SynergyAddCommand;
import org.apache.maven.scm.provider.synergy.command.changelog.SynergyChangeLogCommand;
import org.apache.maven.scm.provider.synergy.command.checkin.SynergyCheckInCommand;
import org.apache.maven.scm.provider.synergy.command.checkout.SynergyCheckOutCommand;
import org.apache.maven.scm.provider.synergy.command.edit.SynergyEditCommand;
import org.apache.maven.scm.provider.synergy.command.remove.SynergyRemoveCommand;
import org.apache.maven.scm.provider.synergy.command.status.SynergyStatusCommand;
import org.apache.maven.scm.provider.synergy.command.tag.SynergyTagCommand;
import org.apache.maven.scm.provider.synergy.command.update.SynergyUpdateCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 *
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="synergy"
 */
public class SynergyScmProvider
    extends AbstractScmProvider
{

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        getLogger().debug( "Creating SynergyScmProviderRepository..." );
        return new SynergyScmProviderRepository( scmSpecificUrl );
    }

    /** {@inheritDoc} */
    public String getScmType()
    {
        return "synergy";
    }

    /** {@inheritDoc} */
    public boolean requiresEditMode()
    {
        return true;
    }

    /** {@inheritDoc} */
    public String getScmSpecificFilename()
    {
        return "_ccmwaid.inf";
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SynergyAddCommand command = new SynergyAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SynergyRemoveCommand command = new SynergyRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        SynergyChangeLogCommand command = new SynergyChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        SynergyCheckInCommand command = new SynergyCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        SynergyCheckOutCommand command = new SynergyCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SynergyEditCommand command = new SynergyEditCommand();

        command.setLogger( getLogger() );

        return (EditScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SynergyUpdateCommand command = new SynergyUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SynergyTagCommand command = new SynergyTagCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        SynergyStatusCommand command = new SynergyStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

}
