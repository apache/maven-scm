package org.apache.maven.scm.provider.git.gitexe.command.info;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Olivier Lamy
 * @since 1.5
 */
public class GitInfoConsumer
    extends AbstractConsumer
{

    //$ git show
    //commit cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767

    private final List<InfoItem> infoItems = new ArrayList<>( 1 );

    private final ScmFileSet scmFileSet;

    public GitInfoConsumer( ScmFileSet scmFileSet )
    {
        this.scmFileSet = scmFileSet;
    }

    /**
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "consume line " + line );
        }

        if ( infoItems.isEmpty() )
        {
            if ( !StringUtils.isEmpty( line ) )
            {
                InfoItem infoItem = new InfoItem();
                infoItem.setRevision( StringUtils.trim( line ) );
                infoItem.setURL( scmFileSet.getBasedir().toPath().toUri().toASCIIString() );
                infoItems.add( infoItem );
            }
        }

    }

    public List<InfoItem> getInfoItems()
    {
        return infoItems;
    }

}
