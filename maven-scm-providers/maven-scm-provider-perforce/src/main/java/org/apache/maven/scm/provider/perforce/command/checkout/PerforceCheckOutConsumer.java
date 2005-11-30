package org.apache.maven.scm.provider.perforce.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.apache.maven.scm.provider.perforce.command.PerforceVerbMapper;
import org.apache.regexp.RE;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceCheckOutConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{
    public static final int STATE_CLIENTSPEC = 0;

    public static final int STATE_NORMAL = 1;

    public static final int STATE_ERROR = 2;

    private int currentState = STATE_CLIENTSPEC;

    private RE fileRegexp = new RE( "([^#]+)#\\d+ - ([a-z]+)" );

    private List checkedout = new ArrayList();

    private String repo = null;

    private String specname = null;

    public PerforceCheckOutConsumer( String clientspec, String repoPath )
    {
        repo = repoPath;
        specname = clientspec;
    }

    /*
     * Client mperham-mikeperham-dt-maven saved.
     */
    /*
     * //depot/modules/cordoba/runtime-ear/.j2ee#1 - deleted as
     * d:\perforce\depot\modules\cordoba\runtime-ear\.j2ee
     * //depot/modules/cordoba/runtime-ear/.project#1 - deleted as
     * d:\perforce\depot\modules\cordoba\runtime-ear\.project
     * //depot/modules/cordoba/runtime-ear/.runtime#1 - deleted as
     * d:\perforce\depot\modules\cordoba\runtime-ear\.runtime
     * //depot/modules/cordoba/runtime-ear/Foo.java#1 - deleted as
     * d:\perforce\depot\modules\cordoba\runtime-ear\Foo.java
     * //depot/modules/cordoba/runtime-ear/META-INF/.modulemaps#1 - deleted as
     * d:\perforce\depot\modules\cordoba\runtime-ear\META-INF\.modulemaps
     * //depot/modules/cordoba/runtime-ear/META-INF/application.xml#1 - deleted
     * as d:\perforce\depot\modules\cordoba\runtime-ear\META-INF\application.xml
     * //depot/modules/cordoba/runtime-ear/pom.xml#4 - deleted as
     * d:\perforce\depot\modules\cordoba\runtime-ear\pom.xml
     */
    /*
     * Invalid changelist/client/label/date '@somelabel'.
     */
    public void consumeLine( String line )
    {
        if ( currentState == STATE_CLIENTSPEC && line.startsWith( "Client " + specname + " saved." ) )
        {
            currentState = STATE_NORMAL;
            return;
        }

        if ( currentState != STATE_ERROR && fileRegexp.match( line ) )
        {
            String location = fileRegexp.getParen( 1 );
            if ( location.startsWith( repo ) )
            {
                location = location.substring( repo.length() + 1 );
            }
            checkedout.add( new ScmFile( location, PerforceVerbMapper.toStatus( fileRegexp.getParen( 2 ) ) ) );
            return;
        }

        error( line );
    }

    private void error( String line )
    {
        currentState = STATE_ERROR;
        output.println( line );
    }

    public boolean isSuccess()
    {
        return currentState == STATE_NORMAL;
    }

    public List getCheckedout()
    {
        return checkedout;
    }
}
