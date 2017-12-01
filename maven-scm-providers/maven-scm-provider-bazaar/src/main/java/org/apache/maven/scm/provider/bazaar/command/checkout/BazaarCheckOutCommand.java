package org.apache.maven.scm.provider.bazaar.command.checkout;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarConstants;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjorn Eikli Smorgrav</a>
 * @author Olivier Lamy
 *
 */
public class BazaarCheckOutCommand
    extends AbstractCheckOutCommand
{
    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        BazaarScmProviderRepository repository = (BazaarScmProviderRepository) repo;
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
        List<String> checkoutCmd = new ArrayList<String>();
        checkoutCmd.add( BazaarConstants.BRANCH_CMD );
        checkoutCmd.add( url );
        checkoutCmd.add( checkoutDir.getAbsolutePath() );
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
             checkoutCmd.add( BazaarConstants.REVISION_OPTION );
             checkoutCmd.add( "tag:" + version.getName() );
        }
        BazaarConsumer checkoutConsumer = new BazaarConsumer( getLogger() );
        BazaarUtils.execute( checkoutConsumer, getLogger(), checkoutDir.getParentFile(),
                             (String[]) checkoutCmd.toArray( new String[0] ) );

        // Do inventory to find list of checkedout files
        String[] inventoryCmd = new String[]{BazaarConstants.INVENTORY_CMD};
        BazaarCheckOutConsumer consumer = new BazaarCheckOutConsumer( getLogger(), checkoutDir );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), checkoutDir, inventoryCmd );
        if ( !result.isSuccess() )
        {
            throw new ScmException( result.getProviderMessage() );
        }
        return new CheckOutScmResult( consumer.getCheckedOutFiles(), result );
    }
}
