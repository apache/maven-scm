package org.apache.maven.scm.provider.perforce.command.edit;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
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
    }

    public boolean isSuccess()
    {
        return !errors;
    }

}
