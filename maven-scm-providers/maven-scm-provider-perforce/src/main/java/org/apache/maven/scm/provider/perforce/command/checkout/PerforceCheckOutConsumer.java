package org.apache.maven.scm.provider.perforce.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.apache.maven.scm.provider.perforce.command.PerforceVerbMapper;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mike Perham
 */
public class PerforceCheckOutConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{
    public static final int STATE_CLIENTSPEC = 0;

    public static final int STATE_NORMAL = 1;

    public static final int STATE_ERROR = 2;

    private int currentState = STATE_CLIENTSPEC;

    private Pattern fileRegexp = Pattern.compile( "([^#]+)#\\d+ - ([a-z]+)" );

    private List<ScmFile> checkedout = new ArrayList<ScmFile>();

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
    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( currentState == STATE_CLIENTSPEC
            && ( line.startsWith( "Client " + specname + " saved." ) || line.startsWith( "Client " + specname
                + " not changed." ) ) )
        {
            currentState = STATE_NORMAL;
            return;
        }

        // Handle case where the clientspec is current
        if ( currentState == STATE_NORMAL && line.indexOf( "ile(s) up-to-date" ) != -1 )
        {
            return;
        }

        Matcher matcher;
        if ( currentState != STATE_ERROR && ( matcher = fileRegexp.matcher( line ) ).find() )
        {
            String location = matcher.group( 1 );
            if ( location.startsWith( repo ) )
            {
                location = location.substring( repo.length() + 1 );
            }
            ScmFileStatus status = PerforceVerbMapper.toStatus( matcher.group( 2 ) );
            if ( status != null )
            {
                // there are cases where Perforce prints out something but the file did not
                // actually change (especially when force syncing).  Those files will have
                // a null status.
                checkedout.add( new ScmFile( location, status ) );
            }
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

    public List<ScmFile> getCheckedout()
    {
        return checkedout;
    }
}
