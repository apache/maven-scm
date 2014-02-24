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
 *
 */
public class PerforceEditConsumer
    extends AbstractPerforceConsumer
    implements StreamConsumer
{

    private static final Pattern PATTERN = Pattern.compile( "^([^#]+)#\\d+ - (.*)" );

    private static final String FILE_BEGIN_TOKEN = "//";

    private List<ScmFile> edits = new ArrayList<ScmFile>();

    private boolean errors = false;
    private StringBuilder errorMessage = new StringBuilder();

    public List<ScmFile> getEdits()
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

        Matcher matcher = PATTERN.matcher( line );
        if ( !matcher.matches() )
        {
            error( line );
        }

        edits.add( new ScmFile( matcher.group( 1 ), ScmFileStatus.EDITED ) );
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
