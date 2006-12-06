package org.apache.maven.scm.provider.synergy.consumer;

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
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * @author <a href="julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyCreateTaskConsumer
    extends AbstractConsumer
{

    private int task;

    /**
     * @return the entries
     */
    public int getTask()
    {
        return task;
    }

    public SynergyCreateTaskConsumer( ScmLogger logger )
    {
        super( logger );
    }

    /**
     * Task 70 created. <br/> The default task is set to: <br/> 70: Maven SCM
     * Synergy provider: adding file(s) to project TestMaven~1
     *
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        getLogger().debug( "Consume: " + line );
        if ( line.startsWith( "Task " ) && line.indexOf( " created." ) > -1 )
        {
            task = Integer.parseInt( line.substring( 5, line.indexOf( " created." ) ) );
        }

    }

}
