package org.apache.maven.scm.provider.hg.command.tag;

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

import org.apache.maven.scm.*;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.apache.maven.scm.provider.hg.command.HgCommand;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.inventory.HgListConsumer;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Tag
 *
 * @author <a href="mailto:ryan@darksleep.com">ryan daum</a>
 * @version $Id$
 */
public class HgTagCommand
        extends AbstractTagCommand
        implements HgCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeTagCommand(ScmProviderRepository scmProviderRepository, ScmFileSet fileSet, String tag, String message) throws ScmException {

        if ( tag == null || StringUtils.isEmpty( tag.trim() ) )
        {
            throw new ScmException( "tag must be specified" );
        }

        if ( fileSet.getFiles().length != 0 )
        {
            throw new ScmException( "This provider doesn't support tagging subsets of a directory" );
        }

        File workingDir = fileSet.getBasedir();


        // build the command
        String[] tagCmd = new String[]{TAG_CMD, MESSAGE_OPTION, message, tag };

        // keep the command about in string form for reporting
        StringBuffer cmd = joinCmd(tagCmd);

        HgTagConsumer consumer = new HgTagConsumer( getLogger() );
        ScmResult result = HgUtils.execute( consumer, getLogger(), workingDir, tagCmd);
        HgScmProviderRepository repository = (HgScmProviderRepository) scmProviderRepository;
        if (result.isSuccess()) {
            // now push
            // Push to parent branch if any
            if ( !repository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) )
            {
                String[] push_cmd = new String[]{HgCommand.PUSH_CMD, repository.getURI()};
                result = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), push_cmd );
            }
        } else {
            throw new ScmException( "Error while executing command " + cmd.toString() );
        }

        // do an inventory to return the files tagged (all of them)
        String[] list_cmd = new String[]{HgCommand.INVENTORY_CMD};
        HgListConsumer listconsumer = new HgListConsumer(getLogger());
        result =  HgUtils.execute(listconsumer, getLogger(), fileSet.getBasedir(), list_cmd);
        if (result.isSuccess()) {
            List files = listconsumer.getFiles();
            ArrayList fileList = new ArrayList();
            for ( Iterator i = files.iterator(); i.hasNext(); )
            {
                ScmFile f = (ScmFile) i.next();

                if (!f.getPath().endsWith(".hgtags"))
                    fileList.add( new ScmFile( f.getPath(), ScmFileStatus.TAGGED ) );
            }

            return new TagScmResult(fileList, result);
        } else {
            throw new ScmException( "Error while executing command " + cmd.toString() );
        }
    }

    private StringBuffer joinCmd(String[] cmd) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < cmd.length; i++) {
            String s = cmd[i];
            result.append(s);
            if (i < cmd.length - 1)
                result.append(" ");
        }
        return result;
    }
}
