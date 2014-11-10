package org.apache.maven.scm.provider.hg.command.inventory;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Get a list of outgoing changes
 *
 * @author <a href="mailto:lperez@xebia.fr">Laurent Perez</a>
 *
 */
public class HgOutgoingConsumer
    extends HgConsumer
{
    private List<HgChangeSet> changes = new ArrayList<HgChangeSet>();

    private static final String BRANCH = "branch";

    public HgOutgoingConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void consumeLine( String line )
    {
        String branch = null;

        if ( line.startsWith( BRANCH ) )
        {
            branch = line.substring( BRANCH.length() + 7 );
        }
        changes.add( new HgChangeSet( branch ) );

    }

    public List<HgChangeSet> getChanges()
    {
        return changes;
    }

}
