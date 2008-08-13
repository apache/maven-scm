package org.apache.maven.scm.provider.perforce.command.edit;

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

import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Perham
 * @version $Id$
 */
public class PerforceEditConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{

    private static final String PATTERN = "^([^#]+)#\\d+ - (.*)";

    private static final String FILE_BEGIN_TOKEN = "//";

    private List edits = new ArrayList();

    private RE revisionRegexp;

    private boolean errors = false;
    private StringBuffer errorMessage = new StringBuffer();

    public PerforceEditConsumer()
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

    public List getEdits()
    {
        return edits;
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( line.startsWith( "... " ) )
        {
            //Should we log this somehow?
            //System.out.println("Perforce: " + line);
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

        edits.add( revisionRegexp.getParen( 1 ) );
    }

    private void error( String line )
    {
        errors = true;
        output.println( line );
        if ( errorMessage.length() > 0 )
        {
            errorMessage.append( System.getProperty( "line.separator" ) );
        }
        errorMessage.append( line );
    }

    public boolean isSuccess()
    {
        return !errors;
    }

    public String getErrorMessage()
    {
        return errorMessage.toString();
    }

}
