package org.apache.maven.scm.manager.plexus;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.AbstractScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.manager.ScmManager"
 */
public class DefaultScmManager
    extends AbstractScmManager
    implements Initializable, LogEnabled
{
    /**
     * @plexus.requirement role="org.apache.maven.scm.provider.ScmProvider"
     */
    private Map<String,ScmProvider> scmProviders;

    private Logger logger;

    // ----------------------------------------------------------------------
    // LogEnabled implementation
    // ----------------------------------------------------------------------

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected void setupLogger( Object component )
    {
        setupLogger( component, logger );
    }

    protected void setupLogger( Object component, String subCategory )
    {
        if ( subCategory == null )
        {
            throw new IllegalStateException( "Logging category must be defined." );
        }

        Logger logger = this.logger.getChildLogger( subCategory );

        setupLogger( component, logger );
    }

    protected void setupLogger( Object component, Logger logger )
    {
        if ( component instanceof LogEnabled )
        {
            ( (LogEnabled) component ).enableLogging( logger );
        }
    }

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void initialize()
    {
        if ( scmProviders == null )
        {
            scmProviders = new HashMap<String,ScmProvider>( 0 );
        }

        if ( getLogger().isWarnEnabled() && scmProviders.size() == 0 )
        {
            getLogger().warn( "No SCM providers configured." );
        }

        setScmProviders( scmProviders );
    }

    /** {@inheritDoc} */
    protected ScmLogger getScmLogger()
    {
        return new PlexusLogger( getLogger() );
    }
}
