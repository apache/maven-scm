package org.apache.maven.scm.provider.dimensionscm.command.checkout;

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
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
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
 * Dimensions CM implementation for Maven's AbstractCheckOutCommand.
 */
public class DimensionsCheckoutCmd extends AbstractCheckOutCommand
{

    private CheckOutScmResult internalExecute( DimensionsScmProviderRepository repository,
        ScmFileSet fileSet, boolean recursive ) throws ScmException,
        TransformerException, ParserConfigurationException, IOException
    {
        String localRepoPath = fileSet.getBasedir().getAbsolutePath();
        String xmlFilePath = XmlFilterUtil.createFilterXml( fileSet );

        String baselineSpec = ParameterUtil.getSystemBaseline( repository.getDmProduct() );

        List<String> checkOutCmd = new Command.Builder( "update", repository )
                .addParameter( "overwrite" )
                .addParameter( recursive ? "recursive" : "norecursive" )
                .addParameter( "stream", StringUtils.isBlank( baselineSpec ) ? repository.getDmProjectSpec() : null )
                .addParameter( "baseline", baselineSpec )
                .addParameter( "user_dir", localRepoPath )
                .addParameter( "directory", repository.getDmDirectoryPath() )
                .addParameter( "user_filter", xmlFilePath )
                .addParameter( "change_doc_ids", ParameterUtil.getSystemRequests() )
                .addParameter( "relative_location", ParameterUtil.getSystemRelativeLocation() )
                .build()
                .getCommandWithLogin();

        boolean ok = CommandExecutor.getInstance().executeCmd( checkOutCmd, getLogger() );

        XmlFilterUtil.deleteFilterXml( xmlFilePath );

        return new CheckOutScmResult(
            "", ok ? DimensionsConstants.COMMAND_SUCCEEDED : DimensionsConstants.COMMAND_FAILED,
            "CHECKOUT completed", ok );
    }

    @Override
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository,
        ScmFileSet fileSet, ScmVersion scmVersion, boolean recursive, boolean shallow ) throws ScmException
    {
        try
        {
            return internalExecute( (DimensionsScmProviderRepository) repository, fileSet, recursive );
        } 
        catch ( Exception e )
        {
            return new CheckOutScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }
}
