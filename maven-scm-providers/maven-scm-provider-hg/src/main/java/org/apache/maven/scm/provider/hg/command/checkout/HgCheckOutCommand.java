package org.apache.maven.scm.provider.hg.command.checkout;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author Olivier Lamy
 *
 */
public class HgCheckOutCommand
    extends AbstractCheckOutCommand
    implements Command
{
    /**
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                       ScmVersion scmVersion, boolean recursive, boolean shallow )
        throws ScmException
    {
        HgScmProviderRepository repository = (HgScmProviderRepository) repo;
        String url = repository.getURI();

        File checkoutDir = fileSet.getBasedir();
        try
        {
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Removing " + checkoutDir );
            }
            FileUtils.deleteDirectory( checkoutDir );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Cannot remove " + checkoutDir );
        }

        // Do the actual checkout
        List<String> cmdList = new ArrayList<String>();
        if ( repo.isPushChanges() )
        {
            cmdList.add( HgCommandConstants.CLONE_CMD );
        }
        else
        {
            cmdList.add( HgCommandConstants.UPDATE_CMD );
        }
        if ( scmVersion != null && !StringUtils.isEmpty( scmVersion.getName() ) )
        {
            cmdList.add( HgCommandConstants.REVISION_OPTION );
            cmdList.add( scmVersion.getName() );
        }
        if ( !repo.isPushChanges() )
        {
            cmdList.add( HgCommandConstants.CLEAN_OPTION );
        }
        cmdList.add( url );
        cmdList.add( checkoutDir.getAbsolutePath() );
        String[] checkoutCmd = cmdList.toArray( new String[0] );
        HgConsumer checkoutConsumer = new HgConsumer( getLogger() );
        HgUtils.execute( checkoutConsumer, getLogger(), checkoutDir.getParentFile(), checkoutCmd );

        // Do inventory to find list of checkedout files
        String[] inventoryCmd = new String[]{ HgCommandConstants.INVENTORY_CMD };
        HgCheckOutConsumer consumer = new HgCheckOutConsumer( getLogger(), checkoutDir );
        ScmResult result = HgUtils.execute( consumer, getLogger(), checkoutDir, inventoryCmd );

        return new CheckOutScmResult( consumer.getCheckedOutFiles(), result );
    }
}
