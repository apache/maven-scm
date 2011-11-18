package org.apache.maven.scm.provider.svn.svnexe.command.remoteinfo;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.remoteinfo.AbstractRemoteInfoCommand;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Olivier Lamy
 * @since 1.6
 */
public class SvnRemoteInfoCommand
    extends AbstractRemoteInfoCommand
    implements SvnCommand
{
    @Override
    public RemoteInfoScmResult executeRemoteInfoCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                         CommandParameters parameters )
        throws ScmException
    {

        String url = ( (SvnScmProviderRepository) repository ).getUrl();
        // use a default svn layout, url is here http://svn.apache.org/repos/asf/maven/maven-3/trunk
        // so as we presume we have good users using standard svn layout, we calculate tags and branches url
        String baseUrl = StringUtils.endsWith( url, "/" )
            ? StringUtils.substringAfter( StringUtils.removeEnd( url, "/" ), "/" )
            : StringUtils.substringBeforeLast( url, "/" );

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( fileSet == null ? null : fileSet.getBasedir(),
                                                                    (SvnScmProviderRepository) repository );

        cl.createArg().setValue( "ls" );

        cl.createArg().setValue( baseUrl + "/tags" );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        LsConsumer consumer = new LsConsumer( getLogger(), baseUrl );

        int exitCode = 0;

        Map<String, String> tagsInfos = null;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
            tagsInfos = consumer.infos;

        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing svn command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new RemoteInfoScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        cl = SvnCommandLineUtils.getBaseSvnCommandLine( fileSet == null ? null : fileSet.getBasedir(),
                                                        (SvnScmProviderRepository) repository );

        cl.createArg().setValue( "ls" );

        cl.createArg().setValue( baseUrl + "/tags" );

        stderr = new CommandLineUtils.StringStreamConsumer();

        consumer = new LsConsumer( getLogger(), baseUrl );

        Map<String, String> branchesInfos = null;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
            branchesInfos = consumer.infos;

        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing svn command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new RemoteInfoScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new RemoteInfoScmResult( cl.toString(), branchesInfos, tagsInfos );
    }


    private static class LsConsumer
        extends AbstractConsumer
    {
        Map<String, String> infos = new HashMap<String, String>();

        String url;

        LsConsumer( ScmLogger logger, String url )
        {
            super( logger );
            this.url = url;
        }

        public void consumeLine( String s )
        {
            infos.put( StringUtils.removeEnd( s, "/" ), url + "/" + s );
        }

        Map<String, String> getInfos()
        {
            return infos;
        }
    }
}
