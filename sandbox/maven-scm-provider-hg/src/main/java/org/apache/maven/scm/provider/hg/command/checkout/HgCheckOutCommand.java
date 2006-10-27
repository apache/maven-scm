package org.apache.maven.scm.provider.hg.command.checkout;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommand;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgCheckOutCommand
    extends AbstractCheckOutCommand
    implements HgCommand
{

    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        HgScmProviderRepository repository = (HgScmProviderRepository) repo;
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
        String[] checkout_cmd = new String[] { BRANCH_CMD, url, checkoutDir.getAbsolutePath() };
        HgConsumer checkout_consumer = new HgConsumer( getLogger() );
        HgUtils.execute( checkout_consumer, getLogger(), checkoutDir.getParentFile(), checkout_cmd );

        // Do inventory to find list of checkedout files
        String[] inventory_cmd = new String[] { INVENTORY_CMD };
        HgCheckOutConsumer consumer = new HgCheckOutConsumer( getLogger(), checkoutDir );
        ScmResult result = HgUtils.execute( consumer, getLogger(), checkoutDir, inventory_cmd );

        return new CheckOutScmResult( consumer.getCheckedOutFiles(), result );
    }
}
