package org.apache.maven.scm.provider.dimensionscm.command.status;

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
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.constants.DimensionsConstants;
import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;
import org.apache.maven.scm.provider.dimensionscm.util.Command;
import org.apache.maven.scm.provider.dimensionscm.util.CommandExecutor;
import org.apache.maven.scm.provider.dimensionscm.util.ParameterUtil;
import org.apache.maven.scm.provider.dimensionscm.util.XmlFilterUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * Dimensions CM implementation for Maven's AbstractStatusCommand.
 */
public class DimensionsStatusCmd extends AbstractStatusCommand
{

    private static final String LOG_FILE_NAME = "changes_log.xml";

    private StatusScmResult internalExecute( DimensionsScmProviderRepository repository,
        ScmFileSet scmFileSet ) throws ScmException,
        TransformerException, ParserConfigurationException
    {
        String localRepoPath = scmFileSet.getBasedir().getAbsolutePath();
        String tmpDir = Paths.get( System.getProperty( "java.io.tmpdir" ), LOG_FILE_NAME ).toString();
        String xmlFilePath = XmlFilterUtil.createFilterXml( scmFileSet );

        String allParam = ParameterUtil.getSystemAll( true ) ? "all" : null;
        String addParam = ParameterUtil.getSystemAdd( true ) ? "add" : "noadd";

        List<String> statusCmd = new Command.Builder( "deliver", repository )
                .addParameter( addParam )
                .addParameter( allParam )
                .addParameter( "delete" )
                .addParameter( "xml" )
                .addParameter( "quiet" )
                .addParameter( "noexecute" )
                .addParameter( "noverbose" )
                .addParameter( "logfile", tmpDir )
                .addParameter( "stream", repository.getDmProjectSpec() )
                .addParameter( "user_dir", localRepoPath )
                .addParameter( "directory", repository.getDmDirectoryPath() )
                .addParameter( "user_filter", xmlFilePath )
                .build()
                .getCommandWithLogin();

        boolean ok = CommandExecutor.getInstance().executeCmdQuiet( statusCmd );

        if ( ok )
        {
            parseXmlFile( tmpDir );
        }
        return new StatusScmResult( "",
            ok ? DimensionsConstants.COMMAND_SUCCEEDED : DimensionsConstants.COMMAND_FAILED, 
            "STATUS completed", ok );
    }

    private void parseXmlFile( String logFilePath ) throws ScmException
    {
        File fXmlFile = new File( logFilePath );

        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse( fXmlFile );
            doc.getDocumentElement().normalize();

            NodeList xmlNodeList = doc.getElementsByTagName( "file-action" );

            for ( int i = 0; i < xmlNodeList.getLength(); i++ )
            {
                String command = "";
                String fileName = "";

                NodeList childList = xmlNodeList.item( i ).getChildNodes();

                for ( int j = 0; j < childList.getLength(); j++ )
                {
                    Node cNode = childList.item( j );

                    String nodeName = cNode.getNodeName();
                    String content = cNode.getTextContent();

                    if ( "path".equalsIgnoreCase( nodeName ) )
                    {
                        fileName = content;
                    }
                    else if ( "command".equalsIgnoreCase( nodeName ) )
                    {
                        command = StringUtils.substringBefore( content, " \"" );
                    }
                }

                switch ( command )
                {
                    case "CI":
                        getLogger().info( String.format( "Created status for \'%s\'", fileName ) );
                        break;
                    case "UI":
                        getLogger().info( String.format( "Modified status for \'%s\'", fileName ) );
                        break;
                    case "RIWS":
                        getLogger().info( String.format( "Deleted status for \'%s\'", fileName ) );
                        break;
                    case "SWF":
                        getLogger().info( String.format( "Renamed/moved status for \'%s\'", fileName ) );
                        break;
                    case "AIWS":
                        getLogger().info( String.format( "Imported status for \'%s\'", fileName ) );
                        break;
                    default:
                        getLogger().info( String.format( "Unknown status for \'%s\'",  fileName ) );
                }
            }

        }
        catch ( Exception e )
        {
            throw new ScmException( "Exception while executing status command: " + e.getMessage() );
        }

        boolean deleted = fXmlFile.delete();

        if ( !deleted )
        {
            throw new ScmException( String.format( "Log file \'%s\' was not deleted successfully.", logFilePath ) );
        }
    }

    @Override
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repository,
        ScmFileSet fileSet ) throws ScmException
    {
        try
        {
            return internalExecute( ( DimensionsScmProviderRepository ) repository, fileSet );
        }
        catch ( Exception e )
        {
            return new StatusScmResult( null, DimensionsConstants.COMMAND_FAILED, e.getMessage(), false );
        }
    }
}
