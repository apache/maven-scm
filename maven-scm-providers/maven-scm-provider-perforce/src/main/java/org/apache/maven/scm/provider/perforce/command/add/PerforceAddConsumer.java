package org.apache.maven.scm.provider.perforce.command.add;

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

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceAddConsumer
    implements StreamConsumer
{

    private static final String PATTERN = "^([^#]+)#1 - (.*)";

    private static final String FILE_BEGIN_TOKEN = "//";

    private List additions = new ArrayList();

    private RE revisionRegexp;

    public PerforceAddConsumer()
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

    public List getAdditions()
    {
        return additions;
    }

    public void consumeLine( String line )
    {
        if ( line.startsWith( "... " ) )
        {
            //TODO log this somehow?
            //System.out.println("Perforce: " + line);
            return;
        }

        if ( !line.startsWith( FILE_BEGIN_TOKEN ) )
        {
            throw new IllegalStateException( "Unknown error: " + line );
        }

        if ( !revisionRegexp.match( line ) )
        {
            throw new IllegalStateException( "Unknown input: " + line );
        }

        additions.add( revisionRegexp.getParen( 1 ) );
    }
}
