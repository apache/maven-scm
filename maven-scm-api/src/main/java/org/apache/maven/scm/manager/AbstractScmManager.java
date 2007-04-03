package org.apache.maven.scm.manager;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractScmManager
    implements ScmManager
{
    private Map scmProviders = new HashMap();

    private ScmLogger logger;

    protected void setScmProviders( Map/*<String,ScmProvider>*/ providers )
    {
        this.scmProviders = providers;
    }

    /**
     * @deprecated use {@link #setScmProvider(String,ScmProvider)} instead
     */
    protected void addScmProvider( String providerType, ScmProvider provider )
    {
        setScmProvider( providerType, provider );
    }

    /**
     * Set a provider to be used for a type of SCM.
     * If there was already a designed provider for that type it will be replaced.
     *
     * @param providerType the type of SCM, eg. <code>svn</code>, <code>cvs</code>
     * @param provider     the provider that will be used for that SCM type
     */
    public void setScmProvider( String providerType, ScmProvider provider )
    {
        scmProviders.put( providerType, provider );
    }

    protected abstract ScmLogger getScmLogger();

    // ----------------------------------------------------------------------
    // ScmManager Implementation
    // ----------------------------------------------------------------------

    public ScmProvider getProviderByUrl( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        String providerType = ScmUrlUtils.getProvider( scmUrl );

        return getProviderByType( providerType );
    }

    public ScmProvider getProviderByType( String providerType )
        throws NoSuchScmProviderException
    {
        if ( logger == null )
        {
            logger = getScmLogger();

            for ( Iterator i = scmProviders.keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();

                ScmProvider p = (ScmProvider) scmProviders.get( key );

                p.addListener( logger );
            }
        }

        String usedProviderType = System.getProperty( "maven.scm.provider." + providerType + ".implementation" );

        if ( usedProviderType == null )
        {
            usedProviderType = providerType;
        }

        ScmProvider scmProvider = (ScmProvider) scmProviders.get( usedProviderType );

        if ( scmProvider == null )
        {
            throw new NoSuchScmProviderException( usedProviderType );
        }

        return scmProvider;
    }

    public ScmProvider getProviderByRepository( ScmRepository repository )
        throws NoSuchScmProviderException
    {
        return getProviderByType( repository.getProvider() );
    }

    // ----------------------------------------------------------------------
    // Repository
    // ----------------------------------------------------------------------

    public ScmRepository makeScmRepository( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        char delimiter = ScmUrlUtils.getDelimiter( scmUrl ).charAt( 0 );

        String providerType = ScmUrlUtils.getProvider( scmUrl );

        ScmProvider provider = getProviderByType( providerType );

        String scmSpecificUrl = cleanScmUrl( scmUrl.substring( providerType.length() + 5 ) );

        ScmProviderRepository providerRepository = provider.makeProviderScmRepository( scmSpecificUrl, delimiter );

        return new ScmRepository( providerType, providerRepository );
    }

    /**
     * Clean the SCM url by removing all ../ in path
     *
     * @param scmUrl the SCM url
     * @return the cleaned SCM url
     */
    protected String cleanScmUrl( String scmUrl )
    {
        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        String pathSeparator = "";

        int indexOfDoubleDot = -1;

        // Clean Unix path
        if ( scmUrl.indexOf( "../" ) > 1 )
        {
            pathSeparator = "/";

            indexOfDoubleDot = scmUrl.indexOf( "../" );
        }

        // Clean windows path
        if ( scmUrl.indexOf( "..\\" ) > 1 )
        {
            pathSeparator = "\\";

            indexOfDoubleDot = scmUrl.indexOf( "..\\" );
        }

        if ( indexOfDoubleDot > 1 )
        {
            int startOfTextToRemove = scmUrl.substring( 0, indexOfDoubleDot - 1 ).lastIndexOf( pathSeparator );

            String beginUrl = "";
            if ( startOfTextToRemove >= 0 )
            {
                beginUrl = scmUrl.substring( 0, startOfTextToRemove );
            }

            String endUrl = scmUrl.substring( indexOfDoubleDot + 3 );

            scmUrl = beginUrl + pathSeparator + endUrl;

            // Check if we have other double dot
            if ( scmUrl.indexOf( "../" ) > 1 || scmUrl.indexOf( "..\\" ) > 1 )
            {
                scmUrl = cleanScmUrl( scmUrl );
            }
        }

        return scmUrl;
    }

    public ScmRepository makeProviderScmRepository( String providerType, File path )
        throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException
    {
        if ( providerType == null )
        {
            throw new NullPointerException( "The provider type cannot be null." );
        }

        ScmProvider provider = getProviderByType( providerType );

        ScmProviderRepository providerRepository = provider.makeProviderScmRepository( path );

        return new ScmRepository( providerType, providerRepository );
    }

    public List validateScmRepository( String scmUrl )
    {
        List messages = new ArrayList();

        messages.addAll( ScmUrlUtils.validate( scmUrl ) );

        String providerType = ScmUrlUtils.getProvider( scmUrl );

        ScmProvider provider;

        try
        {
            provider = getProviderByType( providerType );
        }
        catch ( NoSuchScmProviderException e )
        {
            messages.add( "No such provider installed '" + providerType + "'." );

            return messages;
        }

        String scmSpecificUrl = cleanScmUrl( scmUrl.substring( providerType.length() + 5 ) );

        List providerMessages =
            provider.validateScmUrl( scmSpecificUrl, ScmUrlUtils.getDelimiter( scmUrl ).charAt( 0 ) );

        if ( providerMessages == null )
        {
            throw new RuntimeException( "The SCM provider cannot return null from validateScmUrl()." );
        }

        messages.addAll( providerMessages );

        return messages;
    }

    /**
     *
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).add( repository, fileSet );
    }

    /**
     *
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).add( repository, fileSet, message );
    }

    /**
     *
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startDate, endDate, numDays,
                                                                     branch );
    }

    /**
     *
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch, String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startDate, endDate, numDays,
                                                                     branch, datePattern );
    }

    /**
     *
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                                         ScmVersion endVersion )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startVersion, endVersion );
    }

    /**
     *
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startRevision,
                                         ScmVersion endRevision, String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).changeLog( repository, fileSet, startRevision, endRevision,
                                                                     datePattern );
    }

    /**
     *
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, ScmVersion revision, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkIn( repository, fileSet, revision, message );
    }

    /**
     *
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkOut( repository, fileSet, version );
    }

    /**
     *
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                       boolean recursive )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).checkOut( repository, fileSet, version, recursive );
    }

    /**
     *
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                               ScmVersion endVersion )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).diff( repository, fileSet, startVersion, endVersion );
    }

    /**
     *
     */
    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).edit( repository, fileSet );
    }

    /**
     *
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).export( repository, fileSet, version );
    }

    /**
     *
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String outputDirectory )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).export( repository, fileSet, version, outputDirectory );
    }

    /**
     *
     */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).list( repository, fileSet, recursive, version );
    }

    /**
     *
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).remove( repository, fileSet, message );
    }

    /**
     *
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).status( repository, fileSet );
    }

    /**
     *
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).tag( repository, fileSet, tagName );
    }

    /**
     *
     */
    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).unedit( repository, fileSet );
    }

    /**
     *
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version );
    }

    /**
     *
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   boolean runChangelog )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, runChangelog );
    }

    /**
     *
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version,
                                   String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, datePattern );
    }

    /**
     *
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, lastUpdate );
    }

    /**
     *
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion version, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        return this.getProviderByRepository( repository ).update( repository, fileSet, version, lastUpdate,
                                                                  datePattern );
    }
}
