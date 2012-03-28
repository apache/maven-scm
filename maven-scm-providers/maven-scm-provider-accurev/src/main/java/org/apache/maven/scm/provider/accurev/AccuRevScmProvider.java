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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.cli.AccuRevCommandLine;
import org.apache.maven.scm.provider.accurev.command.add.AccuRevAddCommand;
import org.apache.maven.scm.provider.accurev.command.blame.AccuRevBlameCommand;
import org.apache.maven.scm.provider.accurev.command.changelog.AccuRevChangeLogCommand;
import org.apache.maven.scm.provider.accurev.command.checkin.AccuRevCheckInCommand;
import org.apache.maven.scm.provider.accurev.command.checkout.AccuRevCheckOutCommand;
import org.apache.maven.scm.provider.accurev.command.export.AccuRevExportCommand;
import org.apache.maven.scm.provider.accurev.command.login.AccuRevLoginCommand;
import org.apache.maven.scm.provider.accurev.command.remove.AccuRevRemoveCommand;
import org.apache.maven.scm.provider.accurev.command.status.AccuRevStatusCommand;
import org.apache.maven.scm.provider.accurev.command.tag.AccuRevTagCommand;
import org.apache.maven.scm.provider.accurev.command.update.AccuRevUpdateCommand;
import org.apache.maven.scm.provider.accurev.command.update.AccuRevUpdateScmResult;
import org.apache.maven.scm.provider.accurev.util.QuotedPropertyParser;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.util.StringUtils;

/**
 * AccuRev integration with Maven SCM
 * 
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="accurev"
 */
public class AccuRevScmProvider
    extends AbstractScmProvider
{

    public static final String ACCUREV_EXECUTABLE_PROPERTY = "accurevExe";

    public static final String TAG_FORMAT_PROPERTY = "tagFormat";

    public static final String SYSTEM_PROPERTY_PREFIX = "maven.scm.accurev.";

    public String getScmType()
    {

        return "accurev";
    }

    /**
     * The basic url parsing approach is to be as loose as possible. If you specify as per the docs you'll get what you
     * expect. If you do something else the result is undefined. Don't use "/" "\" or "@" as the delimiter,
     */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {

        List<String> validationMessages = new ArrayList<String>();

        String[] tokens = StringUtils.split( scmSpecificUrl, Character.toString( delimiter ) );

        // [[user][/pass]@host[:port]][:stream][:\project\dir]

        String basisStream = null;
        String projectPath = null;
        int port = AccuRev.DEFAULT_PORT;
        String host = null;
        String user = null;
        String password = null;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( TAG_FORMAT_PROPERTY, AccuRevScmProviderRepository.DEFAULT_TAG_FORMAT );
        properties.put( ACCUREV_EXECUTABLE_PROPERTY, AccuRev.DEFAULT_ACCUREV_EXECUTABLE );

        fillSystemProperties( properties );

        int i = 0;
        while ( i < tokens.length )
        {
            int at = tokens[i].indexOf( '@' );
            // prefer "/", better not have a "/" or a "\\" in your password.
            int slash = tokens[i].indexOf( '/' );
            slash = slash < 0 ? tokens[i].indexOf( '\\' ) : slash;

            int qMark = tokens[i].indexOf( '?' );

            if ( qMark == 0 )
            {
                QuotedPropertyParser.parse( tokens[i].substring( 1 ), properties );
            }
            else if ( slash == 0 )
            {
                // this is the project path
                projectPath = tokens[i].substring( 1 );
                break;
            }
            else if ( ( slash > 0 || ( at >= 0 ) ) && host == null && user == null )
            {
                // user/pass@host
                int len = tokens[i].length();
                if ( at >= 0 && len > at )
                {
                    // everything after the "@"
                    host = tokens[i].substring( at + 1 );
                }

                if ( slash > 0 )
                {
                    // user up to /
                    user = tokens[i].substring( 0, slash );
                    // pass between / and @
                    password = tokens[i].substring( slash + 1, at < 0 ? len : at );
                }
                else
                {
                    // no /, user from beginning to @
                    user = tokens[i].substring( 0, at < 0 ? len : at );
                }

            }
            else if ( host != null && tokens[i].matches( "^[0-9]+$" ) )
            {
                // only valid entry with all digits is the port specification.
                port = Integer.parseInt( tokens[i] );
            }
            else
            {
                basisStream = tokens[i];
            }

            i++;
        }

        if ( i < tokens.length )
        {
            validationMessages.add( "Unknown tokens in URL " + scmSpecificUrl );
        }

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setLogger( getLogger() );
        if ( !StringUtils.isEmpty( user ) )
        {
            repo.setUser( user );
        }
        if ( !StringUtils.isEmpty( password ) )
        {
            repo.setPassword( password );
        }
        if ( !StringUtils.isEmpty( basisStream ) )
        {
            repo.setStreamName( basisStream );
        }
        if ( !StringUtils.isEmpty( projectPath ) )
        {
            repo.setProjectPath( projectPath );
        }
        if ( !StringUtils.isEmpty( host ) )
        {
            repo.setHost( host );
        }
        repo.setPort( port );
        repo.setTagFormat( properties.get( TAG_FORMAT_PROPERTY ) );

        AccuRevCommandLine accuRev = new AccuRevCommandLine( host, port );
        accuRev.setLogger( getLogger() );
        accuRev.setExecutable( properties.get( ACCUREV_EXECUTABLE_PROPERTY ) );
        repo.setAccuRev( accuRev );

        return repo;

    }

    private void fillSystemProperties( Map<String, String> properties )
    {

        Set<String> propertyKeys = properties.keySet();
        for ( String key : propertyKeys )
        {
            String systemPropertyKey = SYSTEM_PROPERTY_PREFIX + key;
            String systemProperty = System.getProperty( systemPropertyKey );
            if ( systemProperty != null )
            {
                properties.put( key, systemProperty );
            }
        }

    }

    @Override
    protected LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( repository.toString() );
        }

        AccuRevLoginCommand command = new AccuRevLoginCommand( getLogger() );
        return command.login( repository, fileSet, parameters );
    }

    @Override
    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                          CommandParameters parameters )
        throws ScmException
    {

        // workaround deprecated behaviour
        // TODO pull up to AbstractScmProvider
        AccuRevScmProviderRepository accuRevRepo = (AccuRevScmProviderRepository) repository;
        if ( !repository.isPersistCheckout() && accuRevRepo.shouldUseExportForNonPersistentCheckout() )
        {

            ExportScmResult result = export( repository, fileSet, parameters );
            if ( result.isSuccess() )
            {
                return new CheckOutScmResult( result.getCommandLine(), result.getExportedFiles(),
                                              accuRevRepo.getExportRelativePath() );
            }
            else
            {
                return new CheckOutScmResult( result.getCommandLine(), result.getProviderMessage(),
                                              result.getCommandOutput(), false );
            }
        }

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        return command.checkout( repository, fileSet, parameters );

    }

    @Override
    protected CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {

        AccuRevCheckInCommand command = new AccuRevCheckInCommand( getLogger() );

        return command.checkIn( repository, fileSet, parameters );
    }

    @Override
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {

        // TODO: accurev info with current dir = "path", find workspace. Find use-case for this.
        return super.makeProviderScmRepository( path );
    }

    @Override
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        AccuRevAddCommand command = new AccuRevAddCommand( getLogger() );
        return command.add( repository, fileSet, parameters );
    }

    @Override
    protected TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        AccuRevTagCommand command = new AccuRevTagCommand( getLogger() );
        return command.tag( repository, fileSet, parameters );

    }

    @Override
    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        AccuRevStatusCommand command = new AccuRevStatusCommand( getLogger() );
        return command.status( repository, fileSet, parameters );

    }

    @Override
    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        AccuRevScmProviderRepository accurevRepo = (AccuRevScmProviderRepository) repository;

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        UpdateScmResult result = command.update( repository, fileSet, parameters );

        if ( result.isSuccess() && parameters.getBoolean( CommandParameter.RUN_CHANGELOG_WITH_UPDATE ) )
        {
            AccuRevUpdateScmResult accuRevResult = (AccuRevUpdateScmResult) result;

            ScmRevision fromRevision = new ScmRevision( accuRevResult.getFromRevision() );
            ScmRevision toRevision = new ScmRevision( accuRevResult.getToRevision() );

            parameters.setScmVersion( CommandParameter.START_SCM_VERSION, fromRevision );
            parameters.setScmVersion( CommandParameter.END_SCM_VERSION, toRevision );

            AccuRevVersion startVersion = accurevRepo.getAccuRevVersion( fromRevision );
            AccuRevVersion endVersion = accurevRepo.getAccuRevVersion( toRevision );
            if ( startVersion.getBasisStream().equals( endVersion.getBasisStream() ) )
            {
                ChangeLogScmResult changeLogResult = changelog( repository, fileSet, parameters );

                if ( changeLogResult.isSuccess() )
                {
                    result.setChanges( changeLogResult.getChangeLog().getChangeSets() );
                }
                else
                {
                    getLogger().warn( "Changelog from " + fromRevision + " to " + toRevision + " failed" );
                }
            }
            else
            {
                String comment = "Cross stream update result from " + startVersion + " to " + endVersion;
                String author = "";
                List<ScmFile> files = result.getUpdatedFiles();
                List<ChangeFile> changeFiles = new ArrayList<ChangeFile>( files.size() );
                for (ScmFile scmFile : files)
                {
                    changeFiles.add(new ChangeFile( scmFile.getPath() ));
                }
                ChangeSet dummyChangeSet = new ChangeSet( new Date(), comment, author, changeFiles );
                // different streams invalidates the change log, insert a dummy change instead.
                List<ChangeSet> changeSets = Collections.singletonList( dummyChangeSet );
                result.setChanges( changeSets );
            }

        }
        return result;
    }

    @Override
    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );
        return command.export( repository, fileSet, parameters );
    }

    @Override
    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                            CommandParameters parameters )
        throws ScmException
    {

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );
        return command.changelog( repository, fileSet, parameters );
    }

    @Override
    protected RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        AccuRevRemoveCommand command = new AccuRevRemoveCommand( getLogger() );
        return command.remove( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    @Override
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        AccuRevBlameCommand blameCommand = new AccuRevBlameCommand( getLogger() );
        return blameCommand.blame( repository, fileSet, parameters );
    }
}
