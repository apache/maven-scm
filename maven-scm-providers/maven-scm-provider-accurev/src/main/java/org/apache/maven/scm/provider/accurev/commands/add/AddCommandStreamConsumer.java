package org.apache.maven.scm.provider.accurev.commands.add;

/*
 * Copyright 2008 AccuRev Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stream consumer for collecting files that were added with the add command
 *
 * @version $Id$
 */
class AddCommandStreamConsumer implements StreamConsumer
{
    private static final Pattern pattern = Pattern.compile( "Added and kept element\\s*(.*)" );

    private final StreamConsumer stdout;
    private final List filesAdded;

    /**
     * @param stdout     The delegating stream consumer
     * @param filesAdded A collection in which added elements should be stored
     */
    public AddCommandStreamConsumer( StreamConsumer stdout, List filesAdded )
    {
        this.stdout = stdout;
        this.filesAdded = filesAdded;
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        stdout.consumeLine( line );

        Matcher m = pattern.matcher( line );
        if ( m.matches() )
        {
            String element = m.group( 1 );
            this.filesAdded.add( element );
        }
    }
}
