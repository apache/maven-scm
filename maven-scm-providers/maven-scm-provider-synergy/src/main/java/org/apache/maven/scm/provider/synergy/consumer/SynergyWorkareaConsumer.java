package org.apache.maven.scm.provider.synergy.consumer;

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

import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;

/**
 * Parse output of
 * 
 * <pre>
 * ccm wa -show -p &lt;project_spec&gt;
 * </pre>
 * 
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyWorkareaConsumer implements StreamConsumer
{
    private ScmLogger logger;

    private File workarea;

    public SynergyWorkareaConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    /**
     * We are expecting the following output:
     * 
     * <pre>
     *            Project Maintain Copies Relative Time Translate Modify Path
     *            -------------------------------------------------------------------
     *            BGZBFZH&tilde;1 TRUE TRUE FALSE FALSE TRUE FALSE 'D:\cmsynergy\ccm_wa\LAPOSTE\BGZBFZH&tilde;1'
     * </pre>
     * 
     * And we want to extract:
     * 
     * <pre>
     *            D:\cmsynergy\ccm_wa\LAPOSTE\BGZBFZH&tilde;1
     *            &lt;pre&gt;
     * 
     */
    public void consumeLine( String line )
    {
        logger.debug( line );
        if ( line.indexOf( " '" ) > -1 )
        {
            int beginIndex = line.indexOf( " '" );
            String fileName = line.substring( beginIndex + 2, line.indexOf( "'", beginIndex + 2 ) );
            workarea = new File( fileName );
        }
    }

    public File getWorkAreaPath()
    {
        return workarea;
    }
}
