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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.apache.maven.scm.util.AbstractConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Mainly inspired from CruiseControl
 *
 * @author <a href="julien.henry@capgemini.com">Julien Henry</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class SynergyGetTaskObjectsConsumer
    extends AbstractConsumer
{

    private List<ChangeFile> entries = new ArrayList<ChangeFile>();

    public static final String OUTPUT_FORMAT = "%name" + SynergyUtil.SEPARATOR + // 0
        "%version" + SynergyUtil.SEPARATOR;

    /**
     * @return the entries
     */
    public List<ChangeFile> getFiles()
    {
        return entries;
    }

    public SynergyGetTaskObjectsConsumer( ScmLogger logger )
    {
        super( logger );
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Consume: " + line );
        }
        StringTokenizer tokenizer = new StringTokenizer( line.trim(), SynergyUtil.SEPARATOR );
        if ( tokenizer.countTokens() == 2 )
        {
            ChangeFile f = new ChangeFile( tokenizer.nextToken() );
            f.setRevision( tokenizer.nextToken() );
            entries.add( f );
        }
        else
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error(
                                   "Invalid token count in SynergyGetTaskObjects [" + tokenizer.countTokens()
                                       + "]" );
            }
        }
    }

}
