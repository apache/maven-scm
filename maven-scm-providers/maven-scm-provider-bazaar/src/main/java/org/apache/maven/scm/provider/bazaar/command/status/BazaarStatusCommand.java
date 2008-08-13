package org.apache.maven.scm.provider.bazaar.command.status;

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
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;

import java.io.File;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 * @version $Id$
 */
public class BazaarStatusCommand
    extends AbstractStatusCommand
    implements BazaarCommand
{

    public BazaarStatusCommand()
    {
        super();
    }

    /** {@inheritDoc} */
    public StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {

        File workingDir = fileSet.getBasedir();
        BazaarStatusConsumer consumer = new BazaarStatusConsumer( getLogger(), workingDir );
        String[] statusCmd = new String[]{STATUS_CMD};
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), workingDir, statusCmd );

        return new StatusScmResult( consumer.getStatus(), result );
    }
}
