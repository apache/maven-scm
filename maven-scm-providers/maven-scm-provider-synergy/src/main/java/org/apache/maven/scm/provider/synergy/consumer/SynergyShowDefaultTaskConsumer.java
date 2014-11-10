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
 * @author <a href="jan.malcomess@steria-mummert.de">Jan Malcomess</a>
 * @since 1.5
 */
public class SynergyShowDefaultTaskConsumer
    extends AbstractConsumer
{

    private int task;

    /**
     * @return the number of the current (ie default) task. 0 if current task 
     *            is not set.
     */
    public int getTask()
    {
        return task;
    }

    public SynergyShowDefaultTaskConsumer( ScmLogger logger )
    {
        super( logger );
    }

    /**
     * Either <br>
     * <code>taskNumber: taskSynopsis</code><br>
     * or <br>
     * <code>The current task is not set.</code><br>
     *
     * {@inheritDoc}
     */
    public void consumeLine( String line )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Consume: " + line );
        }
        if ( !line.contains( "not set" ) )
        {
            task = Integer.parseInt( line.substring( 0, line.indexOf( ':' ) ) );
        }
    }

}
