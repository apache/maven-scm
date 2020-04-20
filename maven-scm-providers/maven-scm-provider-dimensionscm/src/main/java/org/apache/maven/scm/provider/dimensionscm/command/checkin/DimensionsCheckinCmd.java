package org.apache.maven.scm.provider.dimensionscm.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.constants.DimensionsConstants;
import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.util.Command;
import org.apache.maven.scm.provider.dimensionscm.util.CommandExecutor;
import org.apache.maven.scm.provider.dimensionscm.util.ParameterUtil;
import org.apache.maven.scm.provider.dimensionscm.util.XmlFilterUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;

/**
 * Dimensions CM implementation for Maven's AbstractCheckInCommand.
 */
public class DimensionsCheckinCmd extends AbstractCheckInCommand
{

    private CheckInScmResult internalExecute( DimensionsScmProviderRepository repository,
        ScmFileSet scmFileSet, String message ) throws ScmException, TransformerException, ParserConfigurationException
    {
        String localRepoPath = scmFileSet.getBasedir().getAbsolutePath();
        String xmlFilePath = XmlFilterUtil.createFilterXml( scmFileSet );

        String verboseParam = getLogger().isDebugEnabled() ? "verbose" : "noverbose";
        String allParam = ParameterUtil.getSystemAll( false ) ? "all" : null;
        String addParam = ParameterUtil.getSystemAdd( false ) ? "add" : null;

        List<String> checkInCmd = new Command.Builder( "deliver", repository )
                .addParameter( "update" )
                .addParameter( "delete" )
                .addParameter( addParam )
                .addParameter( allParam )
                .addParameter( verboseParam )
                .addParameter( "stream", repository.getDmProjectSpec() )
                .addParameter( "change_doc_ids", ParameterUtil.getSystemRequests() )
                .addParameter( "user_dir", localRepoPath )
                .addParameter( "directory", repository.getDmDirectoryPath() )
                .addParameter( "relative_location", ParameterUtil.getSystemRelativeLocation() )
                .addParameter( "user_filter", xmlFilePath )
                .addParameter( "comment", message )
                .build()
                .getCommandWithLogin();

        boolean ok = CommandExecutor.getInstance().executeCmd( checkInCmd, getLogger() );

        XmlFilterUtil.deleteFilterXml( xmlFilePath );

        return new CheckInScmResult(
            "", ok ? DimensionsConstants.COMMAND_SUCCEEDED : DimensionsConstants.COMMAND_FAILED,
            "CHECKIN completed", ok );
    }

    @Override
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repository,
        ScmFileSet scmFileSet, String message, ScmVersion scmVersion )
    {
        try 
        {
            return internalExecute( (DimensionsScmProviderRepository) repository, scmFileSet, message );
        }
        catch ( Exception e )
        {
            return new CheckInScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }
}
