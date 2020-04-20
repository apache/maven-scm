package org.apache.maven.scm.provider.dimensionscm.command.update;

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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.command.changelog.DimensionsChangelogCmd;
import org.apache.maven.scm.provider.dimensionscm.constants.DimensionsConstants;
import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.util.Command;
import org.apache.maven.scm.provider.dimensionscm.util.CommandExecutor;
import org.apache.maven.scm.provider.dimensionscm.util.ParameterUtil;
import org.apache.maven.scm.provider.dimensionscm.util.XmlFilterUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * Dimensions CM implementation for Maven's AbstractUpdateCommand.
 */
public class DimensionsUpdateCmd extends AbstractUpdateCommand
{

    private UpdateScmResult internalExecute( DimensionsScmProviderRepository repository,
        ScmFileSet scmFileSet ) throws ScmException, TransformerException, ParserConfigurationException, IOException
    {
        String userDirectory = scmFileSet.getBasedir().getAbsolutePath();
        String xmlFilePath = XmlFilterUtil.createFilterXml( scmFileSet );

        String baselineSpec = ParameterUtil.getSystemBaseline( repository.getDmProduct() );

        List<String> updateCmd = new Command.Builder( "update", repository )
                .addParameter( "nooverwrite" )
                .addParameter( "baseline", baselineSpec )
                .addParameter( "stream", StringUtils.isBlank( baselineSpec ) ? repository.getDmProjectSpec() : null )
                .addParameter( "user_dir", userDirectory )
                .addParameter( "directory", repository.getDmDirectoryPath() )
                .addParameter( "user_filter", xmlFilePath )
                .addParameter( "change_doc_ids", ParameterUtil.getSystemRequests() )
                .addParameter( "relative_location", ParameterUtil.getSystemRelativeLocation() )
                .build()
                .getCommandWithLogin();

        boolean ok = CommandExecutor.getInstance().executeCmd( updateCmd, getLogger() );

        XmlFilterUtil.deleteFilterXml( xmlFilePath );

        return new UpdateScmResult( "",
            ok ? DimensionsConstants.COMMAND_SUCCEEDED : DimensionsConstants.COMMAND_FAILED,
            "UPDATE completed", ok );
    }

    @Override
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repository,
        ScmFileSet scmFileSet, ScmVersion scmVersion )
    {
        try
        {
            return internalExecute( ( DimensionsScmProviderRepository ) repository, scmFileSet );
        } 
        catch ( Exception e )
        {
            return new UpdateScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }

    @Override
    protected ChangeLogCommand getChangeLogCommand()
    {
        DimensionsChangelogCmd cmd = new DimensionsChangelogCmd();
        cmd.setLogger( getLogger() );
        return cmd;
    }
}
