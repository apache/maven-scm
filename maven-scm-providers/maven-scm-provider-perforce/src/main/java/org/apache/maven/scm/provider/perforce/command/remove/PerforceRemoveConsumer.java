package org.apache.maven.scm.provider.perforce.command.remove;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Mike Perham
 * @author Olivier Lamy
 *
 */
public class PerforceRemoveConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{
    private static final String FILE_BEGIN_TOKEN = "//";

    private static final String PATTERN = "^([^#]+)#\\d+ - (.*)";

    private List<ScmFile> removals = new ArrayList<ScmFile>();

    private RE revisionRegexp;

    private boolean error = false;

    public PerforceRemoveConsumer()
    {
        try
        {
            revisionRegexp = new RE( PATTERN );
        }
        catch ( RESyntaxException ignored )
        {
            ignored.printStackTrace();
        }
    }

    public List<ScmFile> getRemovals()
    {
        return removals;
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( line.startsWith( "... " ) )
        {
            return;
        }

        if ( !line.startsWith( FILE_BEGIN_TOKEN ) )
        {
            error( line );
        }

        if ( !revisionRegexp.match( line ) )
        {
            error( line );
        }

        removals.add( new ScmFile(revisionRegexp.getParen( 1 ), ScmFileStatus.DELETED ) );
    }

    private void error( String line )
    {
        error = true;
        output.println( line );
    }

    public boolean isSuccess()
    {
        return !error;
    }
}
