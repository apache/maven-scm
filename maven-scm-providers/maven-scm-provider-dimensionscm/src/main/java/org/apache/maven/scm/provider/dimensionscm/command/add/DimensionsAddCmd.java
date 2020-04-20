package org.apache.maven.scm.provider.dimensionscm.command.add;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
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
 * Dimensions CM implementation for Maven's AbstractAddCommand.
 */
public class DimensionsAddCmd extends AbstractAddCommand
{

    private AddScmResult internalExecute( DimensionsScmProviderRepository repository,
        ScmFileSet scmFileSet ) throws ScmException, TransformerException, ParserConfigurationException
    {
        String localRepoPath = scmFileSet.getBasedir().getAbsolutePath();
        String xmlFilePath = XmlFilterUtil.createFilterXml( scmFileSet );

        String verboseParam = getLogger().isDebugEnabled() ? "verbose" : "noverbose";
        String allParam = ParameterUtil.getSystemAll( true ) ? "all" : null;
        String message = ParameterUtil.getSystemMessage();

        List<String> addCmd = new Command.Builder( "deliver", repository )
                .addParameter( "add" )
                .addParameter( allParam )
                .addParameter( "noupdate" )
                .addParameter( "nodelete" )
                .addParameter( verboseParam )
                .addParameter( "change_doc_ids", ParameterUtil.getSystemRequests() )
                .addParameter( "stream", repository.getDmProjectSpec() )
                .addParameter( "user_dir", localRepoPath )
                .addParameter( "directory", repository.getDmDirectoryPath() )
                .addParameter( "user_filter", xmlFilePath )
                .addParameter( "comment", message )
                .build()
                .getCommandWithLogin();


        List<ScmFile> addedFiles = CommandExecutor.getInstance().executeCmdWithParse( addCmd, getLogger() );

        XmlFilterUtil.deleteFilterXml( xmlFilePath );

        return new AddScmResult( "", addedFiles );
    }

    @Override
    protected ScmResult executeAddCommand( ScmProviderRepository repository,
        ScmFileSet fileSet, String message, boolean binary )
    {
        try 
        {
            return internalExecute( (DimensionsScmProviderRepository ) repository, fileSet );
        }
        catch ( Exception e )
        {
            return new AddScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }
}
