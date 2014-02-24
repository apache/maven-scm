package org.apache.maven.scm.provider.perforce.command.unedit;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.perforce.command.AbstractPerforceConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author Mike Perham
 * @author Olivier Lamy
 * @version $Id: PerforceChangeLogConsumer.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceUnEditConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{

    private static final Pattern REVISION_PATTERN = Pattern.compile( "^([^#]+)#\\d+ - (.*)" );

    private static final int STATE_NORMAL = 1;

    private static final int STATE_ERROR = 2;

    private int currentState = STATE_NORMAL;

    private List<ScmFile> edits = new ArrayList<ScmFile>();

    public List<ScmFile> getEdits()
    {
        return edits;
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        Matcher matcher = REVISION_PATTERN.matcher( line );
        if ( currentState != STATE_ERROR && matcher.matches() )
        {
            edits.add( new ScmFile( matcher.group( 1 ), ScmFileStatus.UNKNOWN ) );
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
}
