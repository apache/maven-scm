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

package org.apache.maven.scm.provider.bazaar.command.tag;

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarConstants;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:johan.walles@gmail.com">Johan Walles</a>
 * @version $Id$
 */
public class BazaarTagCommand extends AbstractTagCommand {
    protected ScmResult executeTagCommand( ScmProviderRepository repository,
            ScmFileSet fileSet, String tagName,
            ScmTagParameters scmTagParameters ) throws ScmException
    {
        if ( tagName == null || StringUtils.isEmpty( tagName.trim() ) ) {
            throw new ScmException( "tag name must be specified" );
        }
        
        if ( !fileSet.getFileList().isEmpty() ) {
            throw new ScmException( "tagging specific files is not allowed" );
        }
        
        // Perform the tagging operation
        File bazaarRoot = fileSet.getBasedir();
        BazaarConsumer consumer = new BazaarConsumer( getLogger() );
        String[] tagCmd = new String[] { BazaarConstants.TAG_CMD, tagName };
        ScmResult tagResult = BazaarUtils.execute( consumer, getLogger(), bazaarRoot, tagCmd );
        if ( !tagResult.isSuccess() ) {
            return new TagScmResult( null, tagResult );
        }

        // Do "bzr ls -R -r tag:tagName" to get a list of the tagged files
        BazaarLsConsumer lsConsumer = 
            new BazaarLsConsumer( getLogger(), bazaarRoot, ScmFileStatus.TAGGED );
        String[] lsCmd = new String[] {
                                       BazaarConstants.LS_CMD,
                                       BazaarConstants.RECURSIVE_OPTION,
                                       BazaarConstants.REVISION_OPTION,
                                       "tag:" + tagName
                                       };
        ScmResult lsResult = BazaarUtils.execute(lsConsumer, getLogger(), bazaarRoot, lsCmd);
        if ( !lsResult.isSuccess() ) {
            return new TagScmResult( null, lsResult );
        }
        
        // Push new tags to parent branch if any
        BazaarScmProviderRepository bazaarRepository = (BazaarScmProviderRepository) repository;
        if ( !bazaarRepository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) && repository.isPushChanges() )
        {
            String[] pushCmd = new String[] { BazaarConstants.PUSH_CMD, bazaarRepository.getURI() };
            ScmResult pushResult =
                BazaarUtils.execute( new BazaarConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), pushCmd );
            if ( !pushResult.isSuccess() ) {
                return new TagScmResult( null, pushResult );
            }
        }
        
        return new TagScmResult( lsConsumer.getListedFiles(), tagResult );
    }
}
