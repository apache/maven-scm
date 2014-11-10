package org.apache.maven.scm.provider.vss.commands;

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
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 *
 */
public class VssParameterContext
{

    private String vssPath = null;

    private String autoResponse;

    private String ssDir;

    private String vssLogin;

    private String comment;

    private String user;

    private String fromLabel;

    private String toLabel;

    private boolean quiet;

    private boolean recursive;

    private boolean writable;

    private String label;

    private String style;

    private String version;

    private String date;

    private String localPath;

    private String timestamp;

    /**
     * Behaviour for writable files
     */
    private String writableFiles = null;

    /**
     * From date
     */
    private String fromDate = null;

    /**
     * To date
     */
    private String toDate = null;

    /**
     * Number of days offset for History
     */
    private int numDays = Integer.MIN_VALUE;

    /**
     * Get local copy for checkout defaults to true
     */
    private boolean getLocalCopy = true;

    /**
     * Date format for History
     */
    private DateFormat dateFormat = DateFormat
        .getDateInstance( DateFormat.SHORT );

    private String outputFileName;

    public static VssParameterContext getInstance( Object obj )
    {
        return new VssParameterContext( (VssScmProviderRepository) obj );
    }

    public VssParameterContext( VssScmProviderRepository repo )
    {
        autoResponse = System.getProperty( "maven.scm.autoResponse" );
        this.ssDir = repo.getVssdir();
        this.user = repo.getUser();
//        this.vssLogin = this.user + (repos.getPassword() == null ? "" : ","+repos.getPassword());
    }

    /**
     * Builds and returns the -G- flag if required.
     *
     * @return An empty string if get local copy is true.
     */
    public String getGetLocalCopy()
    {
        return ( !getLocalCopy ) ? VssConstants.FLAG_NO_GET : "";
    }

    /**
     * Calculate the start date for version comparison.
     * <p/>
     * Calculate the date numDay days earlier than startdate.
     *
     * @param startDate The start date.
     * @param daysToAdd The number of days to add.
     * @return The calculated date.
     * @throws ParseException
     */
    private String calcDate( String startDate, int daysToAdd )
        throws ParseException
    {
        Date currentDate = new Date();
        Calendar calendar = new GregorianCalendar();
        currentDate = dateFormat.parse( startDate );
        calendar.setTime( currentDate );
        calendar.add( Calendar.DATE, daysToAdd );
        return dateFormat.format( calendar.getTime() );
    }

    /**
     * Gets the value set for the FileTimeStamp. if it equals "current" then we
     * return -GTC if it equals "modified" then we return -GTM if it equals
     * "updated" then we return -GTU otherwise we return -GTC
     *
     * @return The default file time flag, if not set.
     */
    public String getFileTimeStamp()
    {
        if ( timestamp == null )
        {
            return "";
        }
        return timestamp;
    }

    /**
     * Gets the localpath string. "-GLc:\source"
     * <p/>
     * The localpath is created if it didn't exist.
     *
     * @return An empty string if localpath is not set.
     */
    public String getLocalpath()
        throws ScmException
    {
        String lclPath = ""; // set to empty str if no local path return
        if ( localPath != null )
        {
            // make sure m_LocalDir exists, create it if it doesn't
            File dir = new File( localPath );
            if ( !dir.exists() )
            {
                boolean done = dir.mkdirs();
                if ( !done )
                {
                    String msg = "Directory " + localPath + " creation was not " + "successful for an unknown reason";
                    throw new ScmException( msg );
                }
//                getLogger().info("Created dir: " + dir.getAbsolutePath());
            }
            lclPath = VssConstants.FLAG_OVERRIDE_WORKING_DIR + localPath;
        }
        return lclPath;
    }

    /**
     * Gets the label string. "-Lbuild1" Max label length is 32 chars
     *
     * @return An empty string if label is not set.
     */
    public String getLabel()
    {
        String shortLabel = "";
        if ( label != null && label.length() > 0 )
        {
            shortLabel = VssConstants.FLAG_LABEL + getShortLabel();
        }
        return shortLabel;
    }

    /**
     * Gets the version string. Returns the first specified of version "-V1.0",
     * date "-Vd01.01.01", label "-Vlbuild1".
     *
     * @return An empty string if a version, date and label are not set.
     */
    public String getVersionDateLabel()
    {
        String versionDateLabel = "";
        if ( version != null )
        {
            versionDateLabel = VssConstants.FLAG_VERSION + version;
        }
        else if ( date != null )
        {
            versionDateLabel = VssConstants.FLAG_VERSION_DATE + date;
        }
        else
        {
            // Use getShortLabel() so labels longer then 30 char are truncated
            // and the user is warned
            String shortLabel = getShortLabel();
            if ( shortLabel != null && !shortLabel.equals( "" ) )
            {
                versionDateLabel = VssConstants.FLAG_VERSION_LABEL + shortLabel;
            }
        }
        return versionDateLabel;
    }

    /**
     * Gets the version string.
     *
     * @return An empty string if a version is not set.
     */
    public String getVersion()
    {
        return version != null ? VssConstants.FLAG_VERSION + version : "";
    }

    /**
     * Return at most the 30 first chars of the label, logging a warning message
     * about the truncation
     *
     * @return at most the 30 first chars of the label
     */
    private String getShortLabel()
    {
        String shortLabel;
        if ( label != null && label.length() > 31 )
        {
            shortLabel = this.label.substring( 0, 30 );
//            getLogger().warn(
//                    "Label is longer than 31 characters, truncated to: "
//                            + shortLabel);
        }
        else
        {
            shortLabel = label;
        }
        return shortLabel;
    }

    /**
     * Gets the style string. "-Lbuild1"
     *
     * @return An empty string if label is not set.
     */
    public String getStyle()
    {
        return style != null ? style : "";
    }

    /**
     * Gets the recursive string. "-R"
     *
     * @return An empty string if recursive is not set or is false.
     */
    public String getRecursive()
    {
        return recursive ? VssConstants.FLAG_RECURSION : "";
    }

    /**
     * Gets the writable string. "-W"
     *
     * @return An empty string if writable is not set or is false.
     */
    public String getWritable()
    {
        return writable ? VssConstants.FLAG_WRITABLE : "";
    }

    /**
     * Gets the quiet string. -O-
     *
     * @return An empty string if quiet is not set or is false.
     */
    public String getQuiet()
    {
        return quiet ? VssConstants.FLAG_QUIET : "";
    }

    public String getVersionLabel()
    {
        if ( fromLabel == null && toLabel == null )
        {
            return "";
        }
        if ( fromLabel != null && toLabel != null )
        {
            if ( fromLabel.length() > 31 )
            {
                fromLabel = fromLabel.substring( 0, 30 );
//                getLogger().warn(
//                        "FromLabel is longer than 31 characters, truncated to: "
//                                + fromLabel);
            }
            if ( toLabel.length() > 31 )
            {
                toLabel = toLabel.substring( 0, 30 );
//                getLogger().warn(
//                        "ToLabel is longer than 31 characters, truncated to: "
//                                + toLabel);
            }
            return VssConstants.FLAG_VERSION_LABEL + toLabel + VssConstants.VALUE_FROMLABEL + fromLabel;
        }
        else if ( fromLabel != null )
        {
            if ( fromLabel.length() > 31 )
            {
                fromLabel = fromLabel.substring( 0, 30 );
//                getLogger().warn(
//                        "FromLabel is longer than 31 characters, truncated to: "
//                                + fromLabel);
            }
            return VssConstants.FLAG_VERSION + VssConstants.VALUE_FROMLABEL + fromLabel;
        }
        else
        {
            if ( toLabel.length() > 31 )
            {
                toLabel = toLabel.substring( 0, 30 );
//                getLogger().warn(
//                        "ToLabel is longer than 31 characters, truncated to: "
//                                + toLabel);
            }
            return VssConstants.FLAG_VERSION_LABEL + toLabel;
        }
    }

    /**
     * Gets the user string. "-Uusername"
     *
     * @return An empty string if user is not set.
     */
    public String getUser()
    {
        return user != null ? VssConstants.FLAG_USER + user : "";
    }

    /**
     * Gets the comment string. "-Ccomment text"
     *
     * @return A comment of "-" if comment is not set.
     */
    public String getComment()
    {
        return comment != null ? VssConstants.FLAG_COMMENT + comment : VssConstants.FLAG_COMMENT + "-";
    }

    /**
     * Gets the login string. This can be user and password, "-Yuser,password"
     * or just user "-Yuser".
     *
     * @return An empty string if login is not set.
     */
    public String getLogin()
    {
        return vssLogin != null ? ( VssConstants.FLAG_LOGIN + vssLogin ) : "";
    }

    /**
     * Gets the auto response string. This can be Y "-I-Y" or N "-I-N".
     *
     * @return The default value "-I-" if autoresponse is not set.
     */
    public String getAutoresponse()
    {
        if ( autoResponse == null )
        {
            return VssConstants.FLAG_AUTORESPONSE_DEF;
        }
        else if ( autoResponse.equalsIgnoreCase( "Y" ) )
        {
            return VssConstants.FLAG_AUTORESPONSE_YES;
        }
        else if ( autoResponse.equalsIgnoreCase( "N" ) )
        {
            return VssConstants.FLAG_AUTORESPONSE_NO;
        }
        else
        {
            return VssConstants.FLAG_AUTORESPONSE_DEF;
        }
    }

    /**
     * Gets the sscommand string. "ss" or "c:\path\to\ss"
     *
     * @return The path to ss.exe or just ss if sscommand is not set.
     */
    public String getSSCommand()
    {
        if ( ssDir == null )
        {
            return VssConstants.SS_EXE;
        }
        return ssDir.endsWith( File.separator ) ? ssDir + VssConstants.SS_EXE : ssDir + File.separator
            + VssConstants.SS_EXE;
    }

    public String getVssPath()
    {
        return vssPath;
    }


    /**
     * Gets the Version date string.
     *
     * @return An empty string if neither Todate or from date are set.
     * @throws ScmException
     */
    public String getVersionDate()
        throws ScmException
    {
        if ( fromDate == null && toDate == null && numDays == Integer.MIN_VALUE )
        {
            return "";
        }
        if ( fromDate != null && toDate != null )
        {
            return VssConstants.FLAG_VERSION_DATE + toDate + VssConstants.VALUE_FROMDATE + fromDate;
        }
        else if ( toDate != null && numDays != Integer.MIN_VALUE )
        {
            try
            {
                return VssConstants.FLAG_VERSION_DATE + toDate + VssConstants.VALUE_FROMDATE
                    + calcDate( toDate, numDays );
            }
            catch ( ParseException ex )
            {
                String msg = "Error parsing date: " + toDate;
                throw new ScmException( msg );
            }
        }
        else if ( fromDate != null && numDays != Integer.MIN_VALUE )
        {
            try
            {
                return VssConstants.FLAG_VERSION_DATE + calcDate( fromDate, numDays ) + VssConstants.VALUE_FROMDATE
                    + fromDate;
            }
            catch ( ParseException ex )
            {
                String msg = "Error parsing date: " + fromDate;
                throw new ScmException( msg );
            }
        }
        else
        {
            return fromDate != null ? VssConstants.FLAG_VERSION + VssConstants.VALUE_FROMDATE + fromDate
                            : VssConstants.FLAG_VERSION_DATE + toDate;
        }
    }

    /**
     * Gets the output file string. "-Ooutput.file"
     *
     * @return An empty string if user is not set.
     */
    public String getOutput()
    {
        return outputFileName != null ? VssConstants.FLAG_OUTPUT + outputFileName : "";
    }

    /**
     * Gets the value to determine the behaviour when encountering writable
     * files.
     *
     * @return An empty String, if not set.
     */
    public String getWritableFiles()
    {
        // FIXME: Fix this
        if ( writableFiles == null )
        {
            return "";
        }
        return writableFiles;
    }

}
