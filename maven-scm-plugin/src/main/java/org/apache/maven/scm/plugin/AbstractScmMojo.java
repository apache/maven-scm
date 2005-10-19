package org.apache.maven.scm.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractScmMojo
    extends AbstractMojo
{
    /**
     * The SCM connection URL.
     * 
     * @parameter expression="${connectionUrl}
     */
    private String connectionUrl;

    /**
     * The working directory
     * 
     * @parameter expression="${workingDirectory}"
     */
    private File workingDirectory;

    /**
     * The user name (used by svn protocol).
     * 
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The user password (used by svn protocol).
     * 
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * The url of tags base directory (used by svn protocol).
     * 
     * @parameter expression="${tagBase}"
     */
    private String tagBase;

    /**
     * Comma separated list of includes file pattern.
     * @parameter expression="${includes}" 
     */
    private String includes;

    /**
     * Comma separated list of excludes file pattern.
     * @parameter expression="${excludes}" 
     */
    private String excludes;

    /**
     * @parameter expression="${component.org.apache.maven.scm.manager.ScmManager}"
     * @required
     * @readonly
     */
    private ScmManager manager;

    /**
     * The base directory
     * 
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;

    public String getConnectionUrl()
    {
        if ( connectionUrl == null )
        {
            throw new NullPointerException( "You need to define a connectiuonUrl parameter." );
        }
        return connectionUrl;
    }

    public File getWorkingDirectory()
    {
        if ( workingDirectory == null )
        {
            return basedir;
        }

        return workingDirectory;
    }

    public ScmManager getScmManager()
    {
        return manager;
    }

    public ScmFileSet getFileSet()
        throws IOException
    {
        if ( includes != null || excludes != null )
        {
            return new ScmFileSet( getWorkingDirectory(), includes, excludes );
        }
        else
        {
            return new ScmFileSet( getWorkingDirectory() );
        }
    }

    public ScmRepository getScmRepository()
        throws ScmException
    {
        ScmRepository repository;

        try
        {
            repository = getScmManager().makeScmRepository( getConnectionUrl() );

            getScmManager().getProviderByRepository( repository ).addListener( new DefaultLog( getLog() ) );

            if ( repository.getProvider().equals( "svn" ) )
            {
                SvnScmProviderRepository svnRepo = (SvnScmProviderRepository) repository.getProviderRepository();

                if ( username != null && username.length() > 0 )
                {
                    svnRepo.setUser( username );
                }
                if ( password != null && password.length() > 0 )
                {
                    svnRepo.setPassword( password );
                }
                if ( tagBase != null && tagBase.length() > 0 )
                {
                    svnRepo.setTagBase( tagBase );
                }
            }
            
            if ( repository.getProvider().equals( "starteam" ) )
            {
                StarteamScmProviderRepository starteamRepo = (StarteamScmProviderRepository) repository.getProviderRepository();

                if ( username != null && username.length() > 0 )
                {
                    starteamRepo.setUser( username );
                }
                if ( password != null && password.length() > 0 )
                {
                    starteamRepo.setPassword( password );
                }
            }
            
        }
        catch ( Exception e )
        {
            throw new ScmException( "Can't load the scm provider.", e );
        }

        return repository;
    }

    public void checkResult( ScmResult result )
        throws MojoExecutionException
    {
        if ( !result.isSuccess() )
        {
            getLog().error( "Provider message:" );

            getLog().error( result.getProviderMessage() == null ? "" : result.getProviderMessage() );

            getLog().error( "Command output:" );

            getLog().error( result.getCommandOutput() == null ? "" : result.getCommandOutput() );

            throw new MojoExecutionException( "Command failed." );
        }
    }
}
