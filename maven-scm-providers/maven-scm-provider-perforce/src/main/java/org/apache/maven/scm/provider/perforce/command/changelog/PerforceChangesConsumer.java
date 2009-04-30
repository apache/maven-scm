package org.apache.maven.scm.provider.perforce.command.changelog;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceChangesConsumer
    extends AbstractConsumer
{
    private List entries = new ArrayList();

    /**
     * The regular expression used to match header lines
     */
    private RE revisionRegexp;

    private static final String PATTERN = "^Change (\\d+) " + // changelist number
        "on (.*) " + // date
        "by (.*)@"; // author

    public PerforceChangesConsumer( ScmLogger logger )
    {
        super( logger );

        try
        {
            revisionRegexp = new RE( PATTERN );
        }
        catch ( RESyntaxException ignored )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not create regexp to parse perforce log file", ignored );
            }
        }
    }

    public List getChanges() throws ScmException
    {
        return entries;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if( revisionRegexp.match( line ) )
        {
            entries.add( revisionRegexp.getParen( 1 ) );
        }
    }
}
