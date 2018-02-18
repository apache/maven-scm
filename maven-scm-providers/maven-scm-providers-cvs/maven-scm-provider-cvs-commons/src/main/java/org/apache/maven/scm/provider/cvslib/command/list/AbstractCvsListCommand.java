package org.apache.maven.scm.provider.cvslib.command.list;

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
import java.util.Iterator;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 */
public abstract class AbstractCvsListCommand
    extends AbstractListCommand
    implements CvsCommand
{
    /** {@inheritDoc} */
    protected ListScmResult executeListCommand( ScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
                                                ScmVersion version )
        throws ScmException
    {
        for ( Boolean supD = rlsSUPPORTSD;; supD = Boolean.FALSE )
        {
            ListScmResult res = executeListCommand1( repo, fileSet, recursive, version, supD );
            if ( res.isSuccess() || supD != null )
            {
                if ( rlsSUPPORTSD == null && res.isSuccess() )
                {
                    rlsSUPPORTSD = supD == null ? Boolean.TRUE : supD;
                }
                return res;
            }
            // first attempt failed, support unknown
            // rls: invalid option -- d
        }
    }

    // cvsnt does not support rls -d; Msys/Cygwin do.
    private static Boolean rlsSUPPORTSD = Os.isFamily( Os.FAMILY_WINDOWS ) ? null : Boolean.TRUE;

    private ListScmResult executeListCommand1( ScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
            ScmVersion version, Boolean supD )
    throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "rls", repository, fileSet, "-n" );

        if ( version != null && !StringUtils.isEmpty( version.getName() ) )
        {
            cl.createArg().setValue( "-r" );
            cl.createArg().setValue( version.getName() );
        }

        if ( supD != Boolean.FALSE )
        {
            cl.createArg().setValue( "-d" );
        }
        cl.createArg().setValue( "-e" ); // szakusov: to fix "Unknown file status" problem

        if ( recursive )
        {
            cl.createArg().setValue( "-R" );
        }

        for ( Iterator<File> it = fileSet.getFileList().iterator(); it.hasNext(); )
        {
            File target = (File) it.next();
            String path = target.getPath();
            if ( path.startsWith( "\\" ) )
            {
                path = path.substring( 1 );
            }
            cl.createArg().setValue( path );
        }

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + cl );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        return executeCvsCommand( cl );
    }

    protected abstract ListScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
