package org.apache.maven.scm.provider.jazz.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consume the output of the scm command for the "history" operation.
 * <p>
 * It is passed in a List of ChangeSet entries. All we do is to parse
 * the Jazz change set aliases, and save as the revision into the list.
 * <p>
 * NOTE: We do not set the command or date or anything other than the revision
 * here, as we pick that information up from the "scm list changeset" command.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzHistoryConsumer
    extends AbstractRepositoryConsumer
{
//Change sets:
//  (1589)  ---$ Deb "[maven-release-plugin] prepare for next development itera..."
//  (1585)  ---$ Deb "[maven-release-plugin] prepare release GPDB-1.0.21"
//  (1584)  ---$ Deb "This is my first changeset (2)"
//  (1583)  ---$ Deb "This is my first changeset (1)"

    private static final Pattern CHANGESET_PATTERN = Pattern.compile( "\\((\\d+)\\) (.*)" );

    private List<ChangeSet> entries;

    /**
     * Constructor for our "scm history" consumer.
     *
     * @param repo    The JazzScmProviderRepository being used.
     * @param logger  The ScmLogger to use.
     * @param entries The List of ChangeSet entries that we will populate.
     */
    public JazzHistoryConsumer( ScmProviderRepository repo, ScmLogger logger, List<ChangeSet> entries )
    {
        super( repo, logger );
        this.entries = entries;
    }

    /**
     * Process one line of output from the execution of the "scm xxxx" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );
        Matcher matcher = CHANGESET_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            String changesetAlias = matcher.group( 1 );
            ChangeSet changeSet = new ChangeSet();
            changeSet.setRevision( changesetAlias );

            entries.add( changeSet );
        }
    }
}
