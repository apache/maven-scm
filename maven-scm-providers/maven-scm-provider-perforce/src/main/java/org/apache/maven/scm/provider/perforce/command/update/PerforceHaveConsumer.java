package org.apache.maven.scm.provider.perforce.command.update;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class PerforceHaveConsumer
    extends AbstractConsumer
{
    private String have;

    /**
     * The regular expression used to match header lines
     */
    private static final Pattern REVISION_PATTERN = Pattern.compile( "^Change (\\d+) " + // changelist number
        "on (.*) " + // date
        "by (.*)@" ); // author

    public PerforceHaveConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public String getHave() throws ScmException
    {
        return have;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        Matcher matcher = REVISION_PATTERN.matcher( line );
        if( matcher.find() )
        {
            have = matcher.group( 1 );
        }
    }
}
