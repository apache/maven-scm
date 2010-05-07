package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AccuRevScmProviderRepositoryMatcher
    extends TypeSafeMatcher<ScmProviderRepository>
{

    public static Matcher<ScmProviderRepository> isRepo( String user, String pass, String host, int port,
                                                         String stream, String projectPath )

    {
        return new AccuRevScmProviderRepositoryMatcher( user, pass, host, port, stream, projectPath );
    }

    private String user;

    private String pass;

    private String host;

    private String projectPath;

    private String stream;

    private int port;

    public AccuRevScmProviderRepositoryMatcher( String user, String pass, String host, int port, String stream,
                                                String projectPath )
    {
        this.user = user;
        this.pass = pass;
        this.host = host;
        this.port = port;
        this.stream = stream;
        this.projectPath = projectPath;

    }

    public void describeTo( Description desc )
    {
        desc.appendText( "an AccuRev repo with" );
        desc.appendText( " user=" );
        desc.appendValue( user );
        desc.appendText( " pass=" );
        desc.appendValue( pass );
        desc.appendText( " host=" );
        desc.appendValue( host );
        desc.appendText( " port=" );
        desc.appendValue( port );
        desc.appendText( " stream=" );
        desc.appendValue( stream );
        desc.appendText( " projectPath=" );
        desc.appendValue( projectPath );

    }

    @Override
    public boolean matchesSafely( ScmProviderRepository repo )
    {
        if ( !( repo instanceof AccuRevScmProviderRepository ) )
        {
            return false;
        }
        AccuRevScmProviderRepository accuRevRepo = (AccuRevScmProviderRepository) repo;
        return StringUtils.equals( user, accuRevRepo.getUser() )
            && StringUtils.equals( pass, accuRevRepo.getPassword() )
            && StringUtils.equals( host, accuRevRepo.getHost() ) && port == accuRevRepo.getPort()
            && StringUtils.equals( stream, accuRevRepo.getStreamName() )
            && StringUtils.equals( projectPath, accuRevRepo.getProjectPath() );

    }
}
