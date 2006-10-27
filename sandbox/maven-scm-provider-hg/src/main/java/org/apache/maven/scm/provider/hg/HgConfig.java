package org.apache.maven.scm.provider.hg;

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
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.hg.command.HgCommand;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check hg installation.
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgConfig
{
    //TODO: change BAZAAR to HG, correct versions, and only test for necessary things
    //Minimum version for the Hg SCM
    private static final float BAZAAR_REQ = 0.7f;

    private static final float PYTHON_REQ = 2.4f;

    //Hg spesific
    private static final String BAZAAR_VERSION_TAG = "hg (hg-ng) ";

    private static final String BAZAAR_INSTALL_URL = "'http://hg-vcs.org/Installation'";

    //Python spesific
    private static final String PYTHON_EXEC = "python";

    private static final String PYTHON_VERSION = "-V";

    private static final String PYTHON_VERSION_TAG = "Python ";

    //Python modules
    private static final String PARAMIKO = "\"import paramiko\"";

    private static final String CCRYPT = "\"import Crypto\"";

    private static final String CELEMENTREE = "\"import cElementTree\"";

    //Configuration to check with default values (not installed)
    private VersionConsumer hgVersion = new VersionConsumer( null );

    private VersionConsumer pythonVersion = new VersionConsumer( null );

    private boolean cElementTree = false;

    private boolean paramiko = false;

    private boolean cCrypt = false;

    HgConfig( File workingDir )
    {
        try
        {
            pythonVersion = getPythonVersion( workingDir );
            paramiko = checkPyModules( workingDir, PARAMIKO ); //does not throw
            cCrypt = checkPyModules( workingDir, CCRYPT ); //does not throw
            cElementTree = checkPyModules( workingDir, CELEMENTREE ); //does not throw
            hgVersion = getHgVersion( workingDir );
        }
        catch ( ScmException e )
        {
            //Ignore - Either python and/or hg is not installed.
            //This is already recorded thus we do not generate more info.
        }

    }

    private boolean checkPyModules( File workingDir, String cmd )
    {
        PythonConsumer consumer = new PythonConsumer();
        int exitCode;
        try
        {
            Commandline cmdLine = buildPythonCmd( workingDir, new String[] { "-c", cmd } );
            exitCode = HgUtils.executeCmd( consumer, cmdLine );
        }
        catch ( ScmException e )
        {
            //Ignore - error here is likly to manifest itself when checking python anyway.
            exitCode = -1;
        }

        return exitCode == 0 && consumer.getConsumedAndClear().equals( "" );
    }

    /**
     * @return True if one can run basic hg commands
     */
    private boolean isInstalled()
    {
        return pythonVersion.isVersionOk( PYTHON_REQ ) && hgVersion.isVersionOk( BAZAAR_REQ );
    }

    /**
     * @return True if all modules for hg are installed.
     */
    private boolean isComplete()
    {
        return isInstalled() && cElementTree && paramiko && cCrypt;
    }

    public static VersionConsumer getHgVersion( File workingDir )
        throws ScmException
    {
        String[] versionCmd = new String[] { HgCommand.VERSION };
        VersionConsumer consumer = new VersionConsumer( BAZAAR_VERSION_TAG );
        Commandline cmd = HgUtils.buildCmd( workingDir, versionCmd );

        // Execute command
        HgUtils.executeCmd( consumer, cmd );

        // Return result
        return consumer;
    }

    public static VersionConsumer getPythonVersion( File workingDir )
        throws ScmException
    {
        String[] versionCmd = new String[] { PYTHON_VERSION };
        VersionConsumer consumer = new VersionConsumer( PYTHON_VERSION_TAG );
        Commandline cmd = buildPythonCmd( workingDir, versionCmd );

        // Execute command
        HgUtils.executeCmd( consumer, cmd );

        // Return result
        return consumer;
    }

    private static Commandline buildPythonCmd( File workingDir, String[] cmdAndArgs )
        throws ScmException
    {
        Commandline cmd = new Commandline();
        cmd.setExecutable( PYTHON_EXEC );
        cmd.setWorkingDirectory( workingDir.getAbsolutePath() );
        cmd.addArguments( cmdAndArgs );

        if ( !workingDir.exists() )
        {
            boolean success = workingDir.mkdirs();
            if ( !success )
            {
                String msg = "Working directory did not exist" + " and it couldn't be created: " + workingDir;
                throw new ScmException( msg );
            }
        }
        return cmd;
    }

    /**
     * Get version of the executable.
     * Version is resolved to the last match of a defined regexp in the command output.
     */
    private static class VersionConsumer
        extends HgConsumer
    {

        private static Pattern VERSION_PATTERN = Pattern.compile( "[\\d]+.?[\\d]*" );

        private final String version_tag;

        private String versionStr = "NA";

        private float version = -1;

        VersionConsumer( String version_tag )
        {
            super( new DefaultLog() );
            this.version_tag = version_tag;
        }

        public void doConsume( ScmFileStatus status, String line )
        {
            if ( line.startsWith( version_tag ) )
            {
                versionStr = line.substring( version_tag.length() );
            }
        }

        String getVersion()
        {
            return versionStr;
        }

        boolean isVersionOk( float min )
        {

            Matcher matcher = VERSION_PATTERN.matcher( versionStr );
            if ( matcher.find() )
            {
                String subStr = versionStr.substring( matcher.start(), matcher.end() );
                try
                {
                    version = Float.valueOf( subStr ).floatValue();
                }
                catch ( NumberFormatException e )
                {
                    //Print diagnostics and continue (this is not a major error)
                    getLogger().error( "Regexp for version did not result in a number: " + subStr, e );
                }
            }

            return min <= version;
        }
    }

    private static class PythonConsumer
        extends HgConsumer
    {

        private String consumed = "";

        PythonConsumer()
        {
            super( new DefaultLog() );
        }

        public void doConsume( ScmFileStatus status, String line )
        {
            consumed = line;
        }

        String getConsumedAndClear()
        {
            String tmp = consumed;
            consumed = "";
            return tmp;
        }
    }

    private String getInstalledStr()
    {
        if ( isComplete() )
        {
            return "valid and complete.";
        }
        return ( isInstalled() ? "incomplete. " : "invalid. " ) + "Consult " + BAZAAR_INSTALL_URL;
    }

    public String toString( File workingDir )
    {
        boolean hgOk = hgVersion.isVersionOk( BAZAAR_REQ );
        boolean pyOk = pythonVersion.isVersionOk( PYTHON_REQ );
        return "\n  Your Hg installation seems to be " + getInstalledStr() + "\n    Python version: "
            + pythonVersion.getVersion() + ( pyOk ? " (OK)" : " (May be INVALID)" ) + "\n    Hg version: "
            + hgVersion.getVersion() + ( hgOk ? " (OK)" : " (May be INVALID)" ) + "\n    Paramiko installed: "
            + paramiko + " (For remote access eg. sftp) " + "\n    cCrypt installed: " + cCrypt
            + " (For remote access eg. sftp) " + "\n    cElementTree installed: " + cElementTree + " (Not mandatory) "
            + "\n";
    }
}