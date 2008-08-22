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

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;

/**
 * Get a list of all files in the repository
 *
 * @author <a href="mailto:ryan@darksleep.com">ryan daum</a>
 * @version $Id$
 */
public class HgListCommand
    extends AbstractListCommand
    implements Command
{
    /** {@inheritDoc} */
    protected ListScmResult executeListCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                boolean recursive, ScmVersion scmVersion )
        throws ScmException
    {

        if ( fileSet.getFileList().size() != 0 )
        {
            throw new ScmException( "This provider doesn't support listing subsets of a directory" );
        }
        //
        File workingDir = fileSet.getBasedir();

        // build the command
        String[] listCmd = new String[] { HgCommandConstants.STATUS_CMD, HgCommandConstants.ALL_OPTION };

        // keep the command about in string form for reporting
        StringBuffer cmd = new StringBuffer();
        for ( int i = 0; i < listCmd.length; i++ )
        {
            String s = listCmd[i];
            cmd.append( s );
            if ( i < listCmd.length - 1 )
            {
                cmd.append( " " );
            }
        }

        HgListConsumer consumer = new HgListConsumer( getLogger() );

        ScmResult result = HgUtils.execute( consumer, getLogger(), workingDir, listCmd );

        if ( result.isSuccess() )
        {
            return new ListScmResult( consumer.getFiles(), result );
        }
        else
        {
            throw new ScmException( "Error while executing command " + cmd.toString() );
        }
    }
}
