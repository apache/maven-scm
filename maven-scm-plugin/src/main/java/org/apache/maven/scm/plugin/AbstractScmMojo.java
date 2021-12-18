package org.apache.maven.scm.plugin;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 */
public abstract class AbstractScmMojo
    extends AbstractMojo
{

    protected static final String VERSION_TYPE_BRANCH = "branch";

    protected static final String VERSION_TYPE_REVISION = "revision";

    protected static final String VERSION_TYPE_TAG = "tag";

    protected static final String[] VALID_VERSION_TYPES = { VERSION_TYPE_BRANCH,
            VERSION_TYPE_REVISION, VERSION_TYPE_TAG };

    /**
     * The SCM connection URL.
     */
    @Parameter( property = "connectionUrl", defaultValue = "${project.scm.connection}" )
    private String connectionUrl;

    /**
     * The SCM connection URL for developers.
     */
    @Parameter( property = "developerConnectionUrl", defaultValue = "${project.scm.developerConnection}" )
    private String developerConnectionUrl;

    /**
     * The type of connection to use (connection or developerConnection).
     */
    @Parameter( property = "connectionType", defaultValue = "connection" )
    private String connectionType;

    /**
     * The working directory.
     */
    @Parameter( property = "workingDirectory" )
    private File workingDirectory;

    /**
     * The user name.
     */
    @Parameter( property = "username" )
    private String username;

    /**
     * The user password.
     */
    @Parameter( property = "password" )
    private String password;

    /**
     * The private key.
     */
    @Parameter( property = "privateKey" )
    private String privateKey;

    /**
     * The passphrase.
     */
    @Parameter( property = "passphrase" )
    private String passphrase;

    /**
     * The url of tags base directory (used by svn protocol). It is not
     * necessary to set it if you use the standard svn layout
     * (branches/tags/trunk).
     */
    @Parameter( property = "tagBase" )
    private String tagBase;

    /**
     * Comma separated list of includes file pattern.
     */
    @Parameter( property = "includes" )
    private String includes;

    /**
     * Comma separated list of excludes file pattern.
     */
    @Parameter( property = "excludes" )
    private String excludes;

    @Component
    private ScmManager manager;

    /**
     * When this plugin requires Maven 3.0 as minimum, this component can be removed and o.a.m.s.c.SettingsDecrypter be
     * used instead.
     */
    @Component( hint = "mng-4384" )
    private SecDispatcher secDispatcher;

    /**
     * The base directory.
     */
    @Parameter( property = "basedir", required = true )
    private File basedir;

    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    /**
     * List of System properties to pass to the JUnit tests.
     */
    @Parameter
    private Properties systemProperties;

    /**
     * List of provider implementations.
     */
    @Parameter
    private Map<String, String> providerImplementations;

    /**
     * Should distributed changes be pushed to the central repository?
     * For many distributed SCMs like Git, a change like a commit
     * is only stored in your local copy of the repository.  Pushing
     * the change allows your to more easily share it with other users.
     *
     * @since 1.4
     */
    @Parameter( property = "pushChanges", defaultValue = "true" )
    private boolean pushChanges;

    /**
     * A workItem for SCMs like RTC, TFS etc, that may require additional
     * information to perform a pushChange operation.
     *
     * @since 1.9.5
     */
    @Parameter( property = "workItem" )
    @Deprecated
    private String workItem;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        if ( systemProperties != null )
        {
            // Add all system properties configured by the user
            Iterator<Object> iter = systemProperties.keySet().iterator();

            while ( iter.hasNext() )
            {
                String key = (String) iter.next();

                String value = systemProperties.getProperty( key );

                System.setProperty( key, value );
            }
        }

        if ( providerImplementations != null && !providerImplementations.isEmpty() )
        {
            for ( Entry<String, String> entry : providerImplementations.entrySet() )
            {
                String providerType = entry.getKey();
                String providerImplementation = entry.getValue();
                getLog().info(
                               "Change the default '" + providerType + "' provider implementation to '"
                                   + providerImplementation + "'." );
                getScmManager().setScmProviderImplementation( providerType, providerImplementation );
            }
        }
    }

    protected void setConnectionType( String connectionType )
    {
        this.connectionType = connectionType;
    }

    public String getConnectionUrl()
    {
        boolean requireDeveloperConnection = !"connection".equals( connectionType.toLowerCase() );
        if ( StringUtils.isNotEmpty( connectionUrl ) && !requireDeveloperConnection )
        {
            return connectionUrl;
        }
        else if ( StringUtils.isNotEmpty( developerConnectionUrl ) )
        {
            return developerConnectionUrl;
        }
        if ( requireDeveloperConnection )
        {
            throw new NullPointerException( "You need to define a developerConnectionUrl parameter" );
        }
        else
        {
            throw new NullPointerException( "You need to define a connectionUrl parameter" );
        }
    }

    public void setConnectionUrl( String connectionUrl )
    {
        this.connectionUrl = connectionUrl;
    }

    public File getWorkingDirectory()
    {
        if ( workingDirectory == null )
        {
            return basedir;
        }

        return workingDirectory;
    }

    public File getBasedir()
    {
        return this.basedir;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
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

            providerRepo.setPushChanges( pushChanges );

            if ( !StringUtils.isEmpty( workItem ) )
            {
                providerRepo.setWorkItem( workItem );
            }

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
        catch ( ScmRepositoryException e )
        {
            if ( !e.getValidationMessages().isEmpty() )
            {
                for ( String message : e.getValidationMessages() )
                {
                    getLog().error( message );
                }
            }

            throw new ScmException( "Can't load the scm provider.", e );
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
     * @param repo not null
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
                    username = server.getUsername();
                }

                if ( password == null )
                {
                    password = decrypt( server.getPassword(), host );
                }

                if ( privateKey == null )
                {
                    privateKey = server.getPrivateKey();
                }

                if ( passphrase == null )
                {
                    passphrase = decrypt( server.getPassphrase(), host );
                }
            }
        }
    }

    private String decrypt( String str, String server )
    {
        try
        {
            return secDispatcher.decrypt( str );
        }
        catch ( SecDispatcherException e )
        {
            getLog().warn( "Failed to decrypt password/passphrase for server " + server + ", using auth token as is" );
            return str;
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
                "Command failed: " + Objects.toString( result.getProviderMessage() ) );
        }
    }

    public String getIncludes()
    {
        return includes;
    }

    public void setIncludes( String includes )
    {
        this.includes = includes;
    }

    public String getExcludes()
    {
        return excludes;
    }

    public void setExcludes( String excludes )
    {
        this.excludes = excludes;
    }

    public ScmVersion getScmVersion( String versionType, String version )
        throws MojoExecutionException
    {
        if ( StringUtils.isEmpty( versionType ) && StringUtils.isNotEmpty( version ) )
        {
            throw new MojoExecutionException( "You must specify the version type." );
        }

        if ( StringUtils.isEmpty( version ) )
        {
            return null;
        }

        if ( VERSION_TYPE_BRANCH.equals( versionType ) )
        {
            return new ScmBranch( version );
        }

        if ( VERSION_TYPE_TAG.equals( versionType ) )
        {
            return new ScmTag( version );
        }

        if ( VERSION_TYPE_REVISION.equals( versionType ) )
        {
            return new ScmRevision( version );
        }

        throw new MojoExecutionException( "Unknown '" + versionType + "' version type." );
    }

    protected void handleExcludesIncludesAfterCheckoutAndExport( File checkoutDirectory )
        throws MojoExecutionException
    {
        List<String> includes = new ArrayList<String>();

        if ( ! StringUtils.isBlank( this.getIncludes() ) )
        {
            String[] tokens = StringUtils.split( this.getIncludes(), "," );
            for ( int i = 0; i < tokens.length; ++i )
            {
                includes.add( tokens[i] );
            }
        }

        List<String> excludes = new ArrayList<String>();

        if ( ! StringUtils.isBlank( this.getExcludes() ) )
        {
            String[] tokens = StringUtils.split( this.getExcludes(), "," );
            for ( int i = 0; i < tokens.length; ++i )
            {
                excludes.add( tokens[i] );
            }
        }

        if ( includes.isEmpty() && excludes.isEmpty() )
        {
            return;
        }

        FileSetManager fileSetManager = new FileSetManager();

        FileSet fileset = new FileSet();
        fileset.setDirectory( checkoutDirectory.getAbsolutePath() );
        fileset.setIncludes( excludes ); // revert the order to do the delete
        fileset.setExcludes( includes );
        fileset.setUseDefaultExcludes( false );

        try
        {
            fileSetManager.delete( fileset );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error found while cleaning up output directory base on "
                + "excludes/includes configurations.", e );
        }

    }
}
