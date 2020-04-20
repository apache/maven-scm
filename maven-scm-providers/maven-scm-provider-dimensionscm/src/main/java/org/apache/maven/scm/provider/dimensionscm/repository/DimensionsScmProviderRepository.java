package org.apache.maven.scm.provider.dimensionscm.repository;

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
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.dimensionscm.util.UrlUtil;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.util.regex.Matcher;

/**
 * Dimensions CM implementation of Maven's ScmProviderRepositoryWithHost.
 */
public class DimensionsScmProviderRepository extends ScmProviderRepositoryWithHost
{

    private String dmDatabase; // e.g. "cm_typical@dim10";
    private String dmDatabaseConnection; // e.g. "dim10";
    private String dmDatabaseName; // e.g. "cm_typical"
    private String dmPassword;
    private String dmPort = "671";
    private String dmProduct;
    private String dmProject;
    private String dmProjectSpec;
    private String dmDirectoryPath;
    private String dmServer;
    private String dmUser;

    public DimensionsScmProviderRepository( String url ) throws ScmRepositoryException
    {
        fill( url );
    }

    public String getDmDatabase()
    {
        return dmDatabase;
    }

    public String getDmDatabaseConnection()
    {
        return dmDatabaseConnection;
    }

    public String getDmDatabaseName()
    {
        return dmDatabaseName;
    }

    public String getDmPassword()
    {
        return StringUtils.defaultIfBlank( dmPassword, getPassword() );
    }

    public String getDmPort()
    {
        return dmPort;
    }

    public String getDmProduct()
    {
        return dmProduct;
    }

    public String getDmProject()
    {
        return dmProject;
    }

    public String getDmProjectSpec()
    {
        return dmProjectSpec;
    }

    public String getDmDirectoryPath()
    {
        return dmDirectoryPath;
    }

    public String getDmServer()
    {
        return dmServer;
    }

    public String getHost()
    {
        return dmServer;
    }

    public int getPort()
    {
        return Integer.parseInt( dmPort );
    }

    public String getDmUser()
    {
        return StringUtils.defaultIfBlank( dmUser, getUser() );
    }

    private void fill( String url ) throws ScmRepositoryException
    {
        Matcher urlMatcher = UrlUtil.getMatcher( url );

        dmUser = urlMatcher.group( 1 );
        dmPassword = urlMatcher.group( 2 );
        dmServer = urlMatcher.group( 3 );
        dmPort = StringUtils.defaultIfBlank( urlMatcher.group( 4 ), dmPort );
        dmDatabaseName = urlMatcher.group( 5 );
        dmDatabaseConnection = urlMatcher.group( 6 );
        dmDatabase = String.format( "%s@%s", dmDatabaseName, dmDatabaseConnection );
        dmProduct = urlMatcher.group( 7 );
        dmProject = urlMatcher.group( 8 );
        dmProjectSpec = String.format( "%s:%s", dmProduct, dmProject );
        dmDirectoryPath = urlMatcher.group( 9 );
    }
}
