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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 * @version $Id$
 */
public class BazaarCheckOutCommand
    extends AbstractCheckOutCommand
    implements BazaarCommand
{
    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                        ScmVersion version )
        throws ScmException
    {

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        BazaarScmProviderRepository repository = (BazaarScmProviderRepository) repo;
        String url = repository.getURI();

        File checkoutDir = fileSet.getBasedir();
        try
        {
            getLogger().info( "Removing " + checkoutDir );
            FileUtils.deleteDirectory( checkoutDir );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Cannot remove " + checkoutDir );
        }

        // Do the actual checkout
        String[] checkout_cmd = new String[]{BRANCH_CMD, url, checkoutDir.getAbsolutePath()};
        BazaarConsumer checkout_consumer = new BazaarConsumer( getLogger() );
        BazaarUtils.execute( checkout_consumer, getLogger(), checkoutDir.getParentFile(), checkout_cmd );

        // Do inventory to find list of checkedout files
        String[] inventory_cmd = new String[]{INVENTORY_CMD};
        BazaarCheckOutConsumer consumer = new BazaarCheckOutConsumer( getLogger(), checkoutDir );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), checkoutDir, inventory_cmd );

        return new CheckOutScmResult( consumer.getCheckedOutFiles(), result );
    }
}
