package org.apache.maven.scm.provider.dimensionscm.util;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities for dealing with Dimensions CM filter definition XML.
 */
public class XmlFilterUtil
{

    public static String createFilterXml( ScmFileSet scmFileSet )
        throws ParserConfigurationException, TransformerException
    {

        String includesStr = StringUtils.defaultIfBlank( scmFileSet.getIncludes(), ParameterUtil.getSystemIncludes() );
        String excludesStr = StringUtils.defaultIfBlank( scmFileSet.getExcludes(), ParameterUtil.getSystemExcludes() );

        if ( StringUtils.isBlank( includesStr ) && StringUtils.isBlank( excludesStr ) )
        {
            return StringUtils.EMPTY;
        }
        String[] includes = StringUtils.split( includesStr, "," );
        String[] excludes = StringUtils.split( excludesStr, "," );

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element root = document.createElement( "filter" );
        document.appendChild( root );

        fillRules( includes, document, root, "includes" );
        fillRules( excludes, document, root, "excludes" );

        Path xmlFilePath = Paths.get( System.getProperty( "java.io.tmpdir" ), "user_filters.xml" );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource( document );
        StreamResult streamResult = new StreamResult( xmlFilePath.toFile() );

        transformer.transform( domSource, streamResult );

        return xmlFilePath.toString();
    }


    private static void fillRules( String[] filePatterns, Document document, Element root, String ruleName )
    {
        if ( filePatterns != null )
        {
            Element filterType = document.createElement( ruleName );
            root.appendChild( filterType );

            for ( String filePattern : filePatterns )
            {
                Element rule = document.createElement( "rule" );
                Element pattern = document.createElement( "file-pattern" );
                pattern.setTextContent( filePattern );
                rule.appendChild( pattern );
                filterType.appendChild( rule );
            }
        }
    }

    public static void deleteFilterXml( String path ) throws ScmException
    {

        if ( StringUtils.isBlank( path ) )
        {
            return;
        }
        File file = new File( path );

        if ( !file.delete() )
        {
            throw new ScmException(
                String.format( "Filter configuration file \'%s\' was not deleted successfully.", path ) );
        }
    }

}
