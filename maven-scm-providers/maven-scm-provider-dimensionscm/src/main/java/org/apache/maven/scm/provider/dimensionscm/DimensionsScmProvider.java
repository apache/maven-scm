package org.apache.maven.scm.provider.dimensionscm;

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
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.command.add.DimensionsAddCmd;
import org.apache.maven.scm.provider.dimensionscm.command.changelog.DimensionsChangelogCmd;
import org.apache.maven.scm.provider.dimensionscm.command.checkin.DimensionsCheckinCmd;
import org.apache.maven.scm.provider.dimensionscm.command.checkout.DimensionsCheckoutCmd;
import org.apache.maven.scm.provider.dimensionscm.command.login.DimensionsLoginCmd;
import org.apache.maven.scm.provider.dimensionscm.command.status.DimensionsStatusCmd;
import org.apache.maven.scm.provider.dimensionscm.command.tag.DimensionsTagCmd;
import org.apache.maven.scm.provider.dimensionscm.command.update.DimensionsUpdateCmd;
import org.apache.maven.scm.provider.dimensionscm.constants.DimensionsConstants;
import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.util.UrlUtil;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.util.ArrayList;
import java.util.List;

/**
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="dimensionscm"
 */
public class DimensionsScmProvider extends AbstractScmProvider
{

    public String getScmType()
    {
        return "dimensionscm";
    }


    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl,
        char delimiter ) throws ScmRepositoryException
    {
        return new DimensionsScmProviderRepository( scmSpecificUrl );
    }

    @Override
    protected LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsLoginCmd command = new DimensionsLoginCmd();
        command.setLogger( getLogger() );
        return ( LoginScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsStatusCmd command = new DimensionsStatusCmd();
        command.setLogger( getLogger() );
        return ( StatusScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    protected TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsTagCmd command = new DimensionsTagCmd();
        command.setLogger( getLogger() );
        return ( TagScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsAddCmd command = new DimensionsAddCmd();
        command.setLogger( getLogger() );
        return ( AddScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsChangelogCmd command = new DimensionsChangelogCmd();
        command.setLogger( getLogger() );
        return ( ChangeLogScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    protected CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsCheckinCmd command = new DimensionsCheckinCmd();
        command.setLogger( getLogger() );
        return ( CheckInScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
        CommandParameters parameters ) throws ScmException
    {
        DimensionsCheckoutCmd command = new DimensionsCheckoutCmd();
        command.setLogger( getLogger() );
        return ( CheckOutScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    public UpdateScmResult update( ScmProviderRepository repository,
        ScmFileSet fileSet, CommandParameters parameters ) throws ScmException
    {
        DimensionsUpdateCmd command = new DimensionsUpdateCmd();
        command.setLogger( getLogger() );
        return ( UpdateScmResult ) command.execute( repository, fileSet, parameters );
    }

    @Override
    public List<String> validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        List<String> errorMessages = new ArrayList<>();

        if ( !UrlUtil.isValidUrl( scmSpecificUrl ) )
        {
            errorMessages.add( "The specified url is invalid, it should use this format - "
                + DimensionsConstants.URL_FORMAT );
        }

        return errorMessages;
    }
}
