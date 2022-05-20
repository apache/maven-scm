package org.apache.maven.scm.provider.hg;

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
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * Check hg installation.
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author <a href="mailto:ryan@darksleep.com">ryan daum</a>
 *
 */
public class HgConfig
{
    //Minimum version for the Hg SCM
    private static final String HG_REQ = "0.9.2";

    // The string which indicates the beginning of the Mercurial line
    private static final String HG_VERSION_TAG = "Mercurial Distributed SCM (version ";

    // URL to download mercurial from
    private static final String HG_INSTALL_URL = "'http://www.selenic.com/mercurial/wiki/index.cgi/Download'";

    //Configuration to check with default values (not installed)
    private HgVersionConsumer hgVersion = new HgVersionConsumer( null );

    HgConfig( File workingDir )
    {
        try
        {
            hgVersion = getHgVersion( workingDir );
        }
        catch ( ScmException e )
        {
            //Ignore -  is not installed.
            //This is already recorded thus we do not generate more info.
        }

    }

    /**
     * @return True if one can run basic hg commands
     */
    private boolean isInstalled()
    {
        return hgVersion.isVersionOk( HG_REQ );
    }

    /**
     * @return True if all modules for hg are installed.
     */
    private boolean isComplete()
    {
        return isInstalled();
    }

    // Consumer to find the Mercurial version
    public static HgVersionConsumer getHgVersion( File workingDir )
        throws ScmException
    {
        String[] versionCmd = new String[]{ HgCommandConstants.VERSION };
        HgVersionConsumer consumer = new HgVersionConsumer( HG_VERSION_TAG );
        Commandline cmd = HgUtils.buildCmd( workingDir, versionCmd );

        // Execute command
        HgUtils.executeCmd( consumer, cmd );

        // Return result
        return consumer;
    }


    /**
     * Iterate through two dot-notation version strings, normalize them to the same length, then
     * do alphabetic comparison
     *
     * @param version1
     * @param version2
     * @return true if version2 is greater than version1
     */
    private static boolean compareVersion( String version1, String version2 )
    {
        int l1, l2;
        String v1, v2;

        v1 = version1;
        v2 = version2;
        l1 = version1.length();
        l2 = version2.length();

        if ( l1 > l2 )
        {
            for ( int x = l2; x >= l1; x-- )
            {
                v2 += ' ';
            }
        }
        if ( l2 > l1 )
        {
            for ( int x = l1; x <= l2; x++ )
            {
                v1 += ' ';
            }
        }

        return v2.compareTo( v1 ) >= 0;
    }


    /**
     * Get version of the executable.
     * Version is resolved by splitting the line starting with the version tag and finding
     * the second last word.
     */
    private static class HgVersionConsumer
        extends HgConsumer
    {

        private String versionStr = "NA";

        private String versionTag;

        HgVersionConsumer( String versionTag )
        {
            this.versionTag = versionTag;
        }

        public void doConsume( ScmFileStatus status, String line )
        {
            if ( line.startsWith( versionTag ) )
            {
                String[] elements = line.split( " " );
                versionStr = elements[elements.length - 1].split( "\\)" )[0];
            }
        }

        String getVersion()
        {
            return versionStr;
        }

        boolean isVersionOk( String version )
        {
            // build one number out of the whole version #

            return compareVersion( version, versionStr );
        }
    }

    private String getInstalledStr()
    {
        if ( isComplete() )
        {
            return "valid and complete.";
        }
        return ( isInstalled() ? "incomplete. " : "invalid. " ) + "Consult " + HG_INSTALL_URL;
    }

    public String toString( File workingDir )
    {
        boolean hgOk = hgVersion.isVersionOk( HG_REQ );
        return "\n  Your Hg installation seems to be " + getInstalledStr() + "\n    Hg version: "
            + hgVersion.getVersion() + ( hgOk ? " (OK)" : " (May be INVALID)" ) + "\n";
    }
}
