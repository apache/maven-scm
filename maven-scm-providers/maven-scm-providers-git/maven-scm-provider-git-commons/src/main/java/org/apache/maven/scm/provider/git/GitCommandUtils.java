package org.apache.maven.scm.provider.git;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.apache.maven.scm.providers.gitlib.settings.Settings;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Command utilities for git commands.
 *
 * @author <a href="mailto:jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class GitCommandUtils
{

    private GitCommandUtils()
    {
    }

    public static Commandline getBaseCommand( String commandName, GitScmProviderRepository repo, ScmFileSet fileSet )
    {
        return getBaseCommand( commandName, repo, fileSet, null );
    }

    public static Commandline getBaseCommand( String commandName, GitScmProviderRepository repo, ScmFileSet fileSet,
                                              String options )
    {
        Settings settings = GitUtil.getSettings();

        Commandline cl = new Commandline();

        cl.setExecutable( "git" );

        cl.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        if ( settings.getTraceGitCommand() != null)
        {
            cl.addEnvironment( "GIT_TRACE", settings.getTraceGitCommand() );
        }

        cl.createArgument().setLine( commandName + " " + options );

        return cl;
    }
        
    public static String getRevParseDateFormat()
    {
        return GitUtil.getSettings().getRevParseDateFormat();
    }    
}
