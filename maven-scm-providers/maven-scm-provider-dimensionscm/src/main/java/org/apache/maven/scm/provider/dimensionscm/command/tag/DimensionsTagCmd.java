package org.apache.maven.scm.provider.dimensionscm.command.tag;

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
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
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
 * Dimensions CM implementation for Maven's AbstractTagCommand.
 */
public class DimensionsTagCmd extends AbstractTagCommand
{

    private TagScmResult internalExecute( DimensionsScmProviderRepository repository,
        String tagName, ScmFileSet scmFileSet, ScmTagParameters scmTagParameters ) throws ScmException,
        TransformerException, ParserConfigurationException
    {
        String tagCmdStr = String.format( "cbl %s:%s", repository.getDmProduct(), tagName );
        String xmlFilePath = XmlFilterUtil.createFilterXml( scmFileSet );

        List<String> tagCmd = new Command.Builder( tagCmdStr, repository )
                .addParameter( "scope", "workset" )
                .addParameter( "type", ParameterUtil.getSystemBaselineType() )
                .addParameter( "workset", repository.getDmProjectSpec() )
                .addParameter( "change_doc_ids", ParameterUtil.getSystemRequests() )
                .addParameter( "attributes", ParameterUtil.getSystemAttributes() )
                .addParameter( "user_filter", xmlFilePath )
                .build()
                .getCommandWithLogin();

        boolean ok = CommandExecutor.getInstance().executeCmd( tagCmd, getLogger() );

        return new TagScmResult( "",
            ok ? DimensionsConstants.COMMAND_SUCCEEDED : DimensionsConstants.COMMAND_FAILED, 
            "ADD completed", ok );
    }

    @Override
    protected TagScmResult executeTagCommand( ScmProviderRepository repository,
        ScmFileSet fileSet, String tagName, ScmTagParameters scmTagParameters )
    {
        try
        {
            return internalExecute( ( DimensionsScmProviderRepository ) repository,
                tagName, fileSet, scmTagParameters );
        }
        catch ( Exception e )
        {
            return new TagScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }
}
