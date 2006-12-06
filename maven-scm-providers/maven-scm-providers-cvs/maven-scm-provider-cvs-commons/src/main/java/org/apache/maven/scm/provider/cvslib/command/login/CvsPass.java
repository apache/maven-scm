package org.apache.maven.scm.provider.cvslib.command.login;

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
import org.apache.maven.scm.log.ScmLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Adds an new entry to a CVS password file.
 *
 * @version $Id$
 * @todo Update this class for support password storage in cvsnt. CVSNT use the windows registry, so, we need a jni
 * tool for access to the windows registry
 */
public class CvsPass
{
    /**
     * CVS Root
     */
    private String cvsRoot = null;

    /**
     * Password file to add password to
     */
    private File passFile = null;

    /**
     * Password to add to file
     */
    private String password = null;

    private ScmLogger logger;

    /**
     * Array contain char conversion data
     */
    private final char[] shifts = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, 26, 27, 28, 29, 30, 31, 114, 120, 53, 79, 96, 109, 72, 108, 70, 64, 76, 67, 116, 74, 68, 87, 111, 52,
        75, 119, 49, 34, 82, 81, 95, 65, 112, 86, 118, 110, 122, 105, 41, 57, 83, 43, 46, 102, 40, 89, 38, 103, 45, 50,
        42, 123, 91, 35, 125, 55, 54, 66, 124, 126, 59, 47, 92, 71, 115, 78, 88, 107, 106, 56, 36, 121, 117, 104, 101,
        100, 69, 73, 99, 63, 94, 93, 39, 37, 61, 48, 58, 113, 32, 90, 44, 98, 60, 51, 33, 97, 62, 77, 84, 80, 85, 223,
        225, 216, 187, 166, 229, 189, 222, 188, 141, 249, 148, 200, 184, 136, 248, 190, 199, 170, 181, 204, 138, 232,
        218, 183, 255, 234, 220, 247, 213, 203, 226, 193, 174, 172, 228, 252, 217, 201, 131, 230, 197, 211, 145, 238,
        161, 179, 160, 212, 207, 221, 254, 173, 202, 146, 224, 151, 140, 196, 205, 130, 135, 133, 143, 246, 192, 159,
        244, 239, 185, 168, 215, 144, 139, 165, 180, 157, 147, 186, 214, 176, 227, 231, 219, 169, 175, 156, 206, 198,
        129, 164, 150, 210, 154, 177, 134, 127, 182, 128, 158, 208, 162, 132, 167, 209, 149, 241, 153, 251, 237, 236,
        171, 195, 243, 233, 253, 240, 194, 250, 191, 155, 142, 137, 245, 235, 163, 242, 178, 152};

    /**
     * Create a CVS task using the default cvspass file location.
     */
    public CvsPass( ScmLogger logger )
    {
        passFile = new File( System.getProperty( "cygwin.user.home", System.getProperty( "user.home" ) ) + File
            .separatorChar + ".cvspass" );
        this.logger = logger;
    }

    /**
     * Does the work.
     *
     * @throws ScmException if something is missing
     * @throws IOException  if something goes wrong
     */
    public final void execute()
        throws ScmException, IOException
    {
        if ( cvsRoot == null )
        {
            throw new ScmException( "cvsroot is required" );
        }

        logger.debug( "cvsRoot: " + cvsRoot );

        logger.debug( "passFile: " + passFile );

        BufferedReader reader = null;

        PrintWriter writer = null;
        try
        {
            StringBuffer buf = new StringBuffer();

            if ( passFile.exists() )
            {
                reader = new BufferedReader( new FileReader( passFile ) );

                String line = null;

                while ( ( line = reader.readLine() ) != null )
                {
                    if ( !line.startsWith( cvsRoot ) && !line.startsWith( "/1 " + cvsRoot ) )
                    {
                        buf.append( line ).append( "\n" );
                    }
                    else
                    {
                        logger.debug(
                            "cvsroot " + cvsRoot + " already exist in " + passFile.getAbsolutePath() + ". SKIPPED." );

                        return;
                    }
                }
            }

            if ( password == null )
            {
                throw new ScmException( "password is required. You must run a 'cvs -d " + cvsRoot + " login' first." );
            }

            //logger.debug( "password: " + password );

            String pwdfile = buf.toString() + "/1 " + cvsRoot + " A" + mangle( password );

            logger.debug( "Writing -> " + pwdfile );

            writer = new PrintWriter( new FileWriter( passFile ) );

            writer.println( pwdfile );
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
            if ( writer != null )
            {
                writer.close();
            }
        }
    }

    private final String mangle( String password )
    {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < password.length(); i++ )
        {
            buf.append( shifts[password.charAt( i )] );
        }
        return buf.toString();
    }

    /**
     * The CVS repository to add an entry for.
     *
     * @param cvsRoot the CVS repository
     */
    public void setCvsroot( String cvsRoot )
    {
        this.cvsRoot = cvsRoot;
    }

    /**
     * Password file to add the entry to.
     *
     * @param passFile the password file.
     */
    public void setPassfile( File passFile )
    {
        this.passFile = passFile;
    }

    /**
     * Password to be added to the password file.
     *
     * @param password the password.
     */
    public void setPassword( String password )
    {
        this.password = password;
    }

}
