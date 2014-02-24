package org.apache.maven.scm.provider.jazz.command.changelog;

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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consume the output of the scm command for the "list changesets" operation.
 * <p/>
 * This parses the contents of the output and uses it to fill in the remaining
 * information in the <code>entries</code> list.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzListChangesetConsumer
    extends AbstractRepositoryConsumer
{
//Change sets:
//  (1589)  ---$ Deb "[maven-release-plugin] prepare for next development iteration"
//    Component: (1158) "GPDB"
//    Modified: Feb 25, 2012 10:15 PM (Yesterday)
//    Changes:
//      ---c- (1170) \GPDB\GPDBEAR\pom.xml
//      ---c- (1171) \GPDB\GPDBResources\pom.xml
//      ---c- (1167) \GPDB\GPDBWeb\pom.xml
//      ---c- (1165) \GPDB\pom.xml
//  (1585)  ---$ Deb "[maven-release-plugin] prepare release GPDB-1.0.21"
//    Component: (1158) "GPDB"
//    Modified: Feb 25, 2012 10:13 PM (Yesterday)
//    Changes:
//      ---c- (1170) \GPDB\GPDBEAR\pom.xml
//      ---c- (1171) \GPDB\GPDBResources\pom.xml
//      ---c- (1167) \GPDB\GPDBWeb\pom.xml
//      ---c- (1165) \GPDB\pom.xml
//  (1584)  ---$ Deb "This is my first changeset (2)"
//    Component: (1158) "GPDB"
//    Modified: Feb 25, 2012 10:13 PM (Yesterday)
//  (1583)  ---$ Deb "This is my first changeset (1)"
//    Component: (1158) "GPDB"
//    Modified: Feb 25, 2012 10:13 PM (Yesterday)
//  (1323)  ---$ Deb <No comment>
//    Component: (1158) "GPDB"
//    Modified: Feb 24, 2012 11:04 PM (Last Week)
//    Changes:
//      ---c- (1170) \GPDB\GPDBEAR\pom.xml
//      ---c- (1171) \GPDB\GPDBResources\pom.xml
//      ---c- (1167) \GPDB\GPDBWeb\pom.xml
//      ---c- (1165) \GPDB\pom.xml
//  (1319)  ---$ Deb <No comment>
//    Component: (1158) "GPDB"
//    Modified: Feb 24, 2012 11:03 PM (Last Week)
//    Changes:
//      ---c- (1170) \GPDB\GPDBEAR\pom.xml
//      ---c- (1171) \GPDB\GPDBResources\pom.xml
//      ---c- (1167) \GPDB\GPDBWeb\pom.xml
//      ---c- (1165) \GPDB\pom.xml
//
// NOTE: If the change sets originate on the current date, the date is not
//       displayed, only the time is.
// EG:
//Change sets:
//  (1809)  ---$ Deb "[maven-release-plugin] prepare for next development iteration"
//    Component: (1158) "GPDB"
//    Modified: 6:20 PM (5 minutes ago)
//    Changes:
//      ---c- (1170) \GPDB\GPDBEAR\pom.xml
//      ---c- (1171) \GPDB\GPDBResources\pom.xml
//      ---c- (1167) \GPDB\GPDBWeb\pom.xml
//      ---c- (1165) \GPDB\pom.xml
//  (1801)  ---$ Deb "[maven-release-plugin] prepare release GPDB-1.0.26"
//    Component: (1158) "GPDB"
//    Modified: 6:18 PM (10 minutes ago)
//    Changes:
//      ---c- (1170) \GPDB\GPDBEAR\pom.xml
//      ---c- (1171) \GPDB\GPDBResources\pom.xml
//      ---c- (1167) \GPDB\GPDBWeb\pom.xml
//  (1799)  ---$ Deb <No comment>
//    Component: (1158) "GPDB"
//    Modified: 6:18 PM (10 minutes ago)
//    Changes:
//      ---c- (1165) \GPDB\pom.xml
//  (1764)  ---$ Deb <No comment>
//    Component: (1158) "GPDB"
//    Modified: Mar 1, 2012 2:34 PM
//    Changes:
//      ---c- (1165) \GPDB\pom.xml


    // State Machine Definitions
    private static final int STATE_CHANGE_SETS = 0;

    private static final int STATE_CHANGE_SET = 1;

    private static final int STATE_COMPONENT = 2;

    private static final int STATE_MODIFIED = 3;

    private static final int STATE_CHANGES = 4;

    // Header definitions. 
    private static final String HEADER_CHANGE_SETS = "Change sets:";

    private static final String HEADER_CHANGE_SET = "(";

    private static final String HEADER_COMPONENT = "Component:";

    private static final String HEADER_MODIFIED = "Modified:";

    private static final String HEADER_CHANGES = "Changes:";

    private static final String JAZZ_TIMESTAMP_PATTERN = "MMM d, yyyy h:mm a";
    // Actually: DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT );

    private static final String JAZZ_TIMESTAMP_PATTERN_TIME = "h:mm a";
    // Only seen when the data = today. Only the time is displayed.

    //  (1589)  ---$ Deb "[maven-release-plugin] prepare for next development iteration"
    //  (1585)  ---$ Deb "[maven-release-plugin] prepare release GPDB-1.0.21"
    private static final Pattern CHANGESET_PATTERN = Pattern.compile( "\\((\\d+)\\)  (....) (\\w+) (.*)" );

    //      ---c- (1170) \GPDB\GPDBEAR\pom.xml
    //      ---c- (1171) \GPDB\GPDBResources\pom.xml
    //      ---c- (1167) \GPDB\GPDBWeb\pom.xml
    //      ---c- (1165) \GPDB\pom.xml
    private static final Pattern CHANGES_PATTERN = Pattern.compile( "(.....) \\((\\d+)\\) (.*)" );


    private List<ChangeSet> entries;

    private final String userDateFormat;

    // This is incremented at the beginning of every change set line. So we start at -1 (to get zero on first processing)
    private int currentChangeSetIndex = -1;

    private int currentState = STATE_CHANGE_SETS;

    /**
     * Constructor for our "scm list changeset" consumer.
     *
     * @param repo    The JazzScmProviderRepository being used.
     * @param logger  The ScmLogger to use.
     * @param entries The List of ChangeSet entries that we will populate.
     */
    public JazzListChangesetConsumer( ScmProviderRepository repo, ScmLogger logger, List<ChangeSet> entries,
                                      String userDateFormat )
    {
        super( repo, logger );
        this.entries = entries;
        this.userDateFormat = userDateFormat;
    }

    /**
     * Process one line of output from the execution of the "scm list changeset" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );

        // Process the "Change sets:" line - do nothing
        if ( line.trim().startsWith( HEADER_CHANGE_SETS ) )
        {
            currentState = STATE_CHANGE_SETS;
        }
        else
        {
            if ( line.trim().startsWith( HEADER_CHANGE_SET ) )
            {
                currentState = STATE_CHANGE_SET;
            }
            else
            {
                if ( line.trim().startsWith( HEADER_COMPONENT ) )
                {
                    currentState = STATE_COMPONENT;
                }
                else
                {
                    if ( line.trim().startsWith( HEADER_MODIFIED ) )
                    {
                        currentState = STATE_MODIFIED;
                    }
                    else
                    {
                        if ( line.trim().startsWith( HEADER_CHANGES ) )
                        {
                            // Note: processChangesLine() will also be passed the "Changes:" line
                            // So, it needs to be able to deal with that.
                            // Changes:
                            //   ---c- (1170) \GPDB\GPDBEAR\pom.xml
                            //   ---c- (1171) \GPDB\GPDBResources\pom.xml
                            //   ---c- (1167) \GPDB\GPDBWeb\pom.xml
                            //   ---c- (1165) \GPDB\pom.xml
                            currentState = STATE_CHANGES;
                        }
                    }
                }
            }
        }

        switch ( currentState )
        {
            case STATE_CHANGE_SETS:
                // Nothing to do.
                break;

            case STATE_CHANGE_SET:
                processChangeSetLine( line );
                break;

            case STATE_COMPONENT:
                // Nothing to do. Not used (Yet?)
                break;

            case STATE_MODIFIED:
                processModifiedLine( line );
                break;

            case STATE_CHANGES:
                processChangesLine( line );
                break;
        }

    }

    private void processChangeSetLine( String line )
    {
        // Process the headerless change set line - starts with a '(', eg:
        // (1589)  ---$ Deb "[maven-release-plugin] prepare for next development iteration"
        // (1585)  ---$ Deb "[maven-release-plugin] prepare release GPDB-1.0.21"
        Matcher matcher = CHANGESET_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            // This is the only place this gets incremented.
            // It starts at -1, and on first execution is incremented to 0 - which is correct.
            currentChangeSetIndex++;
            ChangeSet currentChangeSet = entries.get( currentChangeSetIndex );

            // Init the file of files, so it is not null, but it can be empty!
            List<ChangeFile> files = new ArrayList<ChangeFile>();
            currentChangeSet.setFiles( files );

            String changesetAlias = matcher.group( 1 );
            String changeFlags = matcher.group( 2 );     // Not used.
            String author = matcher.group( 3 );
            String comment = matcher.group( 4 );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "  Parsing ChangeSet Line : " + line );
                getLogger().debug( "    changesetAlias : " + changesetAlias );
                getLogger().debug( "    changeFlags    : " + changeFlags );
                getLogger().debug( "    author         : " + author );
                getLogger().debug( "    comment        : " + comment );
            }

            // Sanity check.
            if ( currentChangeSet.getRevision() != null && !currentChangeSet.getRevision().equals( changesetAlias ) )
            {
                getLogger().warn( "Warning! The indexes appear to be out of sequence! " +
                                      "For currentChangeSetIndex = " + currentChangeSetIndex + ", we got '" +
                                      changesetAlias + "' and not '" + currentChangeSet.getRevision()
                                      + "' as expected." );
            }

            comment = stripDelimiters( comment );
            currentChangeSet.setAuthor( author );
            currentChangeSet.setComment( comment );
        }
    }

    private void processModifiedLine( String line )
    {
        // Process the "Modified: ..." line, eg:
        // Modified: Feb 25, 2012 10:15 PM (Yesterday)
        // Modified: Feb 25, 2012 10:13 PM (Yesterday)
        // Modified: Feb 24, 2012 11:03 PM (Last Week)
        // Modified: Mar 1, 2012 2:34 PM
        // Modified: 6:20 PM (5 minutes ago)

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "  Parsing Modified Line : " + line );
        }

        int colonPos = line.indexOf( ":" );
        int parenPos = line.indexOf( "(" );

        String date = null;

        if ( colonPos != -1 && parenPos != -1 )
        {
            date = line.substring( colonPos + 2, parenPos - 1 );
        }
        else
        {
            if ( colonPos != -1 && parenPos == -1 )
            {
                // No trailing bracket
                date = line.substring( colonPos + 2 );
            }
        }

        if ( date != null )
        {
            Date changesetDate = parseDate( date.toString(), userDateFormat, JAZZ_TIMESTAMP_PATTERN );
            // try again forcing en locale
            if ( changesetDate == null )
            {
                changesetDate = parseDate( date.toString(), userDateFormat, JAZZ_TIMESTAMP_PATTERN, Locale.ENGLISH );
            }
            if ( changesetDate == null )
            {
                // changesetDate will be null when the date is not given, it only has just the time. The date is today.
                changesetDate = parseDate( date.toString(), userDateFormat, JAZZ_TIMESTAMP_PATTERN_TIME );
                // Get today's time/date. Used to get the date.
                Calendar today = Calendar.getInstance();
                // Get a working one.
                Calendar changesetCal = Calendar.getInstance();
                // Set the date/time. Used to set the time.
                changesetCal.setTimeInMillis( changesetDate.getTime() );
                // Now set the date (today).
                changesetCal.set( today.get( Calendar.YEAR ), today.get( Calendar.MONTH ),
                                  today.get( Calendar.DAY_OF_MONTH ) );
                // Now get the date of the combined results.
                changesetDate = changesetCal.getTime();
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "    date           : " + date );
                getLogger().debug( "    changesetDate  : " + changesetDate );
            }

            ChangeSet currentChangeSet = entries.get( currentChangeSetIndex );
            currentChangeSet.setDate( changesetDate );
        }
    }

    private void processChangesLine( String line )
    {
        // Process the changes line, eg:
        //      ---c- (1170) \GPDB\GPDBEAR\pom.xml
        //      ---c- (1171) \GPDB\GPDBResources\pom.xml
        //      ---c- (1167) \GPDB\GPDBWeb\pom.xml
        //      ---c- (1165) \GPDB\pom.xml
        Matcher matcher = CHANGES_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            ChangeSet currentChangeSet = entries.get( currentChangeSetIndex );

            String changeFlags = matcher.group( 1 );     // Not used.
            String fileAlias = matcher.group( 2 );
            String file = matcher.group( 3 );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "  Parsing Changes Line : " + line );
                getLogger().debug(
                    "    changeFlags    : " + changeFlags + " Translated to : " + parseFileChangeState( changeFlags ) );
                getLogger().debug( "    filetAlias     : " + fileAlias );
                getLogger().debug( "    file           : " + file );
            }

            ChangeFile changeFile = new ChangeFile( file );
            ScmFileStatus status = parseFileChangeState( changeFlags );
            changeFile.setAction( status );
            currentChangeSet.getFiles().add( changeFile );
        }
    }

    /**
     * String the leading/trailing ", < and > from the text.
     *
     * @param text The text to process.
     * @return The striped text.
     */
    protected String stripDelimiters( String text )
    {
        if ( text == null )
        {
            return null;
        }
        String workingText = text;
        if ( workingText.startsWith( "\"" ) || workingText.startsWith( "<" ) )
        {
            workingText = workingText.substring( 1 );
        }
        if ( workingText.endsWith( "\"" ) || workingText.endsWith( ">" ) )
        {
            workingText = workingText.substring( 0, workingText.length() - 1 );
        }

        return workingText;
    }

    /**
     * Parse the change state file flags from Jazz and map them to the maven SCM ones.
     * <p/>
     * "----" Character positions 0-3.
     * <p/>
     * [0] is '*' or '-'    Indicates that this is the current change set ('*') or not ('-').   STATE_CHANGESET_CURRENT
     * [1] is '!' or '-'    Indicates a Potential Conflict ('!') or not ('-').                  STATE_POTENTIAL_CONFLICT
     * [2] is '#' or '-'    Indicates a Conflict ('#') or not ('-').                            STATE_CONFLICT
     * [3] is '@' or '$'    Indicates whether the changeset is active ('@') or not ('$').       STATE_CHANGESET_ACTIVE
     *
     * @param state The 5 character long state string
     * @return The ScmFileStatus value.
     */
    private ScmFileStatus parseChangeSetChangeState( String state )
    {
        if ( state.length() != 4 )
        {
            throw new IllegalArgumentException( "Change State string must be 4 chars long!" );
        }

        // This is not used, but is here for potential future usage and for documentation purposes.
        return ScmFileStatus.UNKNOWN;
    }

    /**
     * Parse the change state file flags from Jazz and map them to the maven SCM ones.
     * <p/>
     * "-----" Character positions 0-4. The default is '-'.
     * <p/>
     * [0] is '-' or '!'    Indicates a Potential Conflict. STATE_POTENTIAL_CONFLICT
     * [1] is '-' or '#'    Indicates a Conflict.           STATE_CONFLICT
     * [2] is '-' or 'a'    Indicates an addition.          STATE_ADD
     * or 'd'    Indicates a deletion.           STATE_DELETE
     * or 'm'    Indicates a move.               STATE_MOVE
     * [3] is '-' or 'c'    Indicates a content change.     STATE_CONTENT_CHANGE
     * [4] is '-' or 'p'    Indicates a property change.    STATE_PROPERTY_CHANGE
     * <p/>
     * NOTE: [3] and [4] can only be set it [2] is NOT 'a' or 'd'.
     *
     * @param state The 5 character long state string
     * @return The SCMxxx value.
     */
    private ScmFileStatus parseFileChangeState( String state )
    {
        if ( state.length() != 5 )
        {
            throw new IllegalArgumentException( "Change State string must be 5 chars long!" );
        }

        // NOTE: We have an impedance mismatch here. The Jazz file change flags represent
        // many different states. However, we can only return *ONE* ScmFileStatus value,
        // so we need to be careful as to the precedence that we give to them.

        ScmFileStatus status = ScmFileStatus.UNKNOWN;   // Probably not a valid initial default value.

        // [0] is '-' or '!'    Indicates a Potential Conflict. STATE_POTENTIAL_CONFLICT
        if ( state.charAt( 0 ) == '!' )
        {
            status = ScmFileStatus.CONFLICT;
        }
        // [1] is '-' or '#'    Indicates a Conflict.           STATE_CONFLICT
        if ( state.charAt( 1 ) == '#' )
        {
            status = ScmFileStatus.CONFLICT;
        }

        // [2] is '-' or 'a'    Indicates an addition.          STATE_ADD
        //            or 'd'    Indicates a deletion.           STATE_DELETE
        //            or 'm'    Indicates a move.               STATE_MOVE
        if ( state.charAt( 2 ) == 'a' )
        {
            status = ScmFileStatus.ADDED;
        }
        else
        {
            if ( state.charAt( 2 ) == 'd' )
            {
                status = ScmFileStatus.DELETED;
            }
            else
            {
                if ( state.charAt( 2 ) == 'm' )
                {
                    status = ScmFileStatus.RENAMED;     // Has been renamed or moved.
                }

                // [3] is '-' or 'c'    Indicates a content change.     STATE_CONTENT_CHANGE
                if ( state.charAt( 3 ) == 'c' )
                {
                    status = ScmFileStatus.MODIFIED;    // The file has been modified in the working tree.
                }

                // [4] is '-' or 'p'    Indicates a property change.    STATE_PROPERTY_CHANGE
                if ( state.charAt( 4 ) == 'p' )
                {
                    status =
                        ScmFileStatus.MODIFIED;    // ScmFileStatus has no concept of property or meta data changes.
                }
            }
        }

        return status;
    }
}
