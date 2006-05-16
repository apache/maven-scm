package org.apache.maven.scm.plugin;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;

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
     * @parameter expression="${connectionUrl}" default-value="${project.scm.connection}"
     */
    private String connectionUrl;

    /**
     * @parameter expression="${connectionUrl}" default-value="${project.scm.developerConnection}"
     */
    private String developerConnectionUrl;

    /**
     * The type of connection to use (connection or developerConnection).
     *
     * @parameter expression="${connectionType}" default-value="connection"
     */
    private String connectionType;

    /**
     * The working directory
     *
     * @parameter expression="${workingDirectory}"
     */
    private File workingDirectory;

    /**
     * The user name (used by svn and starteam protocol).
     *
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The user password (used by svn and starteam protocol).
     *
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * The private key (used by java svn).
     *
     * @parameter expression="${privateKey}"
     */
    private String privateKey;

    /**
     * The passphrase (used by java svn).
     *
     * @parameter expression="${passphrase}"
     */
    private String passphrase;

    /**
     * The url of tags base directory (used by svn protocol). Not necessary to set it if you use standard svn layout (branches/tags/trunk).
     *
     * @parameter expression="${tagBase}"
     */
    private String tagBase;

    /**
     * Comma separated list of includes file pattern.
     *
     * @parameter expression="${includes}"
     */
    private String includes;

    /**
     * Comma separated list of excludes file pattern.
     *
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

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    protected void setConnectionType( String connectionType )
    {
        this.connectionType = connectionType;
    }

    public String getConnectionUrl()
    {
        if ( StringUtils.isNotEmpty( connectionUrl ) && "connection".equals( connectionType.toLowerCase() ) )
        {
            return connectionUrl;
        }
        else if ( StringUtils.isNotEmpty( developerConnectionUrl ) )
        {
            return developerConnectionUrl;
        }

        throw new NullPointerException( "You need to define a connectionUrl parameter" );
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

            ScmProviderRepository providerRepo = repository.getProviderRepository();

            if ( !StringUtils.isEmpty( username ) )
            {
                providerRepo.setUser( username );
            }

            if ( !StringUtils.isEmpty( password ) )
            {
                providerRepo.setPassword( password );
            }

            if ( repository.getProviderRepository() instanceof ScmProviderRepositoryWithHost )
            {
                ScmProviderRepositoryWithHost repo = (ScmProviderRepositoryWithHost) repository.getProviderRepository();

                loadInfosFromSettings( repo );

                if ( !StringUtils.isEmpty( username ) )
                {
                    repo.setUser( username );
                }

                if ( !StringUtils.isEmpty( password ) )
                {
                    repo.setPassword( password );
                }

                if ( !StringUtils.isEmpty( privateKey ) )
                {
                    repo.setPrivateKey( privateKey );
                }

                if ( !StringUtils.isEmpty( passphrase ) )
                {
                    repo.setPassphrase( passphrase );
                }
            }

            if ( !StringUtils.isEmpty( tagBase ) && repository.getProvider().equals( "svn" ) )
            {
                SvnScmProviderRepository svnRepo = (SvnScmProviderRepository) repository.getProviderRepository();

                svnRepo.setTagBase( tagBase );
            }
        }
        catch ( Exception e )
        {
            throw new ScmException( "Can't load the scm provider.", e );
        }

        return repository;
    }

    /**
     * Load username password from settings if user has not set them in JVM properties
     *
     * @param repo
     */
    private void loadInfosFromSettings( ScmProviderRepositoryWithHost repo )
    {
        if ( username == null || password == null )
        {
            String host = repo.getHost();

            int port = repo.getPort();

            if ( port > 0 )
            {
                host += ":" + port;
            }

            Server server = this.settings.getServer( host );

            if ( server != null )
            {
                if ( username == null )
                {
                    username = this.settings.getServer( host ).getUsername();
                }

                if ( password == null )
                {
                    password = this.settings.getServer( host ).getPassword();
                }

                if ( privateKey == null )
                {
                    privateKey = this.settings.getServer( host ).getPrivateKey();
                }

                if ( passphrase == null )
                {
                    passphrase = this.settings.getServer( host ).getPassphrase();
                }
            }
        }
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

            throw new MojoExecutionException(
                "Command failed." + StringUtils.defaultString( result.getProviderMessage() ) );
        }
    }
}
