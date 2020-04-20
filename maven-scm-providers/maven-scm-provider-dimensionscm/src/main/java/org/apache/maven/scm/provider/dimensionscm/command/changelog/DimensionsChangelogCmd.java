package org.apache.maven.scm.provider.dimensionscm.command.changelog;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.constants.DimensionsConstants;
import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.util.Command;
import org.apache.maven.scm.provider.dimensionscm.util.CommandExecutor;
import org.apache.maven.scm.provider.dimensionscm.util.ParameterUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dimensions CM implementation for Maven's AbstractChangeLogCommand.
 */
public class DimensionsChangelogCmd extends AbstractChangeLogCommand
{

    private ChangeLogScmResult internalExecute( DimensionsScmProviderRepository repository,
        Date startDate, Date endDate ) throws ScmException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        String fromDate = startDate != null ? simpleDateFormat.format( startDate ) : StringUtils.EMPTY;
        String toDate = endDate != null ? simpleDateFormat.format( endDate ) : StringUtils.EMPTY;

        String filenameParam = ParameterUtil.getSystemFilename();
        String directoryPathParam = StringUtils.isBlank(
            repository.getDmDirectoryPath() ) ? "" : repository.getDmDirectoryPath().concat( "/**" );

        List<String> changelogCmd = new Command.Builder( "log", repository )
                .addParameter( "from_time", fromDate )
                .addParameter( "to_time", toDate )
                .addParameter( "filename", StringUtils.isBlank( filenameParam ) ? directoryPathParam : filenameParam )
                .addParameter( "workset", repository.getDmProjectSpec() )
                .build()
                .getCommandWithLogin();

        boolean ok = CommandExecutor.getInstance().executeCmd( changelogCmd, getLogger() );

        if ( !ok )
        {
            throw new ScmException( "Changelog command execution was failed." );
        }

        return new ChangeLogScmResult( "", new ChangeLogSet( new ArrayList<>(), startDate, endDate ) );
    }

    @Override
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
        Date startDate, Date endDate, ScmBranch branch, String datePattern ) throws ScmException
    {
        try
        {
            return internalExecute( (DimensionsScmProviderRepository) repository, startDate, endDate );
        } 
        catch ( Exception e )
        {
            return new ChangeLogScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }
}
