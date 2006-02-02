package org.apache.maven.scm;

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

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ChangeSet
{
    /**
     * Escaped <code>&lt;</code> entity
     */
    public static final String LESS_THAN_ENTITY = "&lt;";

    /**
     * Escaped <code>&gt;</code> entity
     */
    public static final String GREATER_THAN_ENTITY = "&gt;";

    /**
     * Escaped <code>&amp;</code> entity
     */
    public static final String AMPERSAND_ENTITY = "&amp;";

    /**
     * Escaped <code>'</code> entity
     */
    public static final String APOSTROPHE_ENTITY = "&apos;";

    /**
     * Escaped <code>"</code> entity
     */
    public static final String QUOTE_ENTITY = "&quot;";

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Formatter used by the getDateFormatted method.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( DATE_PATTERN );

    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * Formatter used by the getTimeFormatted method.
     */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat( TIME_PATTERN );

    /**
     * Formatter used to parse date/timestamp.
     */
    private static final SimpleDateFormat TIMESTAMP_FORMAT_1 = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );

    private static final SimpleDateFormat TIMESTAMP_FORMAT_2 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    /**
     * Date the changes were committed
     */
    private Date date;

    /**
     * User who made changes
     */
    private String author;

    /**
     * comment provided at commit time
     */
    private String comment = "";

    private List files;

    public ChangeSet( String date, String userDatePattern, String comment, String author, List files )
    {
        setDate( date, userDatePattern );

        setAuthor( author );

        setComment( comment );

        this.files = files;
    }

    /**
     * Constructor used when attributes aren't available until later
     */
    public ChangeSet()
    {
    }

    /**
     * Getter for ChangeFile list.
     *
     * @return List of ChangeFile.
     */
    public List getFiles()
    {
        return files;
    }

    /**
     * Setter for ChangeFile list.
     *
     * @param files List of ChangeFiles.
     */
    public void setFiles( List files )
    {
        this.files = files;
    }

    public void addFile( ChangeFile file )
    {
        if ( files == null )
        {
            files = new ArrayList();
        }

        files.add( file );
    }

    public boolean containsFilename( String filename, ScmProviderRepository repository )
    {
        if ( files != null )
        {
            for ( Iterator i = files.iterator(); i.hasNext(); )
            {
                ChangeFile file = (ChangeFile) i.next();

                if ( file.getName().equals( filename ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Getter for property author.
     *
     * @return Value of property author.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Setter for property author.
     *
     * @param author New value of property author.
     */
    public void setAuthor( String author )
    {
        this.author = author;
    }

    /**
     * Getter for property comment.
     *
     * @return Value of property comment.
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Setter for property comment.
     *
     * @param comment New value of property comment.
     */
    public void setComment( String comment )
    {
        this.comment = comment;
    }

    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public Date getDate()
    {
        if ( date != null )
        {
            return (Date) date.clone();
        }

        return null;
    }

    /**
     * Setter for property date.
     *
     * @param date New value of property date.
     */
    public void setDate( Date date )
    {
        this.date = new Date( date.getTime() );
    }

    /**
     * Setter for property date that takes a string and parses it
     *
     * @param date - a string in yyyy/MM/dd HH:mm:ss format
     */
    public void setDate( String date )
    {
        setDate( date, null );
    }

    /**
     * Setter for property date that takes a string and parses it
     *
     * @param date            - a string in yyyy/MM/dd HH:mm:ss format
     * @param userDatePattern - pattern of date
     */
    public void setDate( String date, String userDatePattern )
    {
        try
        {
            if ( !StringUtils.isEmpty( userDatePattern ) )
            {
                SimpleDateFormat format = new SimpleDateFormat( userDatePattern );
                this.date = format.parse( date );
            }
            else
            {
                this.date = TIMESTAMP_FORMAT_1.parse( date );
            }
        }
        catch ( ParseException e )
        {
            if ( !StringUtils.isEmpty( userDatePattern ) )
            {
                try
                {
                    this.date = TIMESTAMP_FORMAT_2.parse( date );
                }
                catch ( ParseException pe )
                {
                    try
                    {
                        this.date = TIMESTAMP_FORMAT_2.parse( date );
                    }
                    catch ( ParseException ex )
                    {
                        throw new IllegalArgumentException( "Unable to parse date: " + date );
                    }
                }
            }
            else
            {
                try
                {
                    this.date = TIMESTAMP_FORMAT_2.parse( date );
                }
                catch ( ParseException ex )
                {
                    throw new IllegalArgumentException( "Unable to parse date: " + date );
                }
            }
        }
    }

    /**
     * @return date in yyyy-mm-dd format
     */
    public synchronized String getDateFormatted()
    {
        return DATE_FORMAT.format( getDate() );
    }

    /**
     * @return time in HH:mm:ss format
     */
    public synchronized String getTimeFormatted()
    {
        return TIME_FORMAT.format( getDate() );
    }

    /**
     * @return Returns string representation of the changeset
     */
    public String toString()
    {
        String result = author + "\n" + date + "\n";

        if ( files != null )
        {
            for ( Iterator i = files.iterator(); i.hasNext(); )
            {
                ChangeFile file = (ChangeFile) i.next();

                result += file + "\n";
            }
        }

        result += comment;

        return result;
    }

    /**
     * Provide the changelog entry as an XML snippet.
     *
     * @return a changelog-entry in xml format
     * @task make sure comment doesn't contain CDATA tags - MAVEN114
     */
    public String toXML()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "\t<changelog-entry>\n" );

        if ( getDate() != null )
        {
            buffer.append( "\t\t<date pattern=\"" + DATE_PATTERN + "\">" )
                .append( getDateFormatted() )
                .append( "</date>\n" )
                .append( "\t\t<time pattern=\"" + TIME_PATTERN + "\">" )
                .append( getTimeFormatted() )
                .append( "</time>\n" );
        }

        buffer.append( "\t\t<author><![CDATA[" )
            .append( author )
            .append( "]]></author>\n" );

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            ChangeFile file = (ChangeFile) i.next();
            buffer.append( "\t\t<file>\n" )
                .append( "\t\t\t<name>" )
                .append( escapeValue( file.getName() ) )
                .append( "</name>\n" )
                .append( "\t\t\t<revision>" )
                .append( file.getRevision() )
                .append( "</revision>\n" );
            buffer.append( "\t\t</file>\n" );
        }
        buffer.append( "\t\t<msg><![CDATA[" )
            .append( removeCDataEnd( comment ) )
            .append( "]]></msg>\n" );
        buffer.append( "\t</changelog-entry>\n" );

        return buffer.toString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ChangeSet )
        {
            ChangeSet changeSet = (ChangeSet) obj;

            if ( toString().equals( changeSet.toString() ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * remove a <code>]]></code> from comments (replace it with <code>] ] ></code>).
     *
     * @param message
     * @return a clean string
     */
    private String removeCDataEnd( String message )
    {
        // check for invalid sequence ]]>
        int endCdata;
        while ( message != null && ( endCdata = message.indexOf( "]]>" ) ) > -1 )
        {
            message = message.substring( 0, endCdata ) + "] ] >" + message.substring( endCdata + 3, message.length() );
        }
        return message;
    }

    /**
     * <p>Escape the <code>toString</code> of the given object.
     * For use in an attribute value.</p>
     * <p/>
     * swiped from jakarta-commons/betwixt -- XMLUtils.java
     *
     * @param value escape <code>value.toString()</code>
     * @return text with characters restricted (for use in attributes) escaped
     */
    public static String escapeValue( Object value )
    {
        StringBuffer buffer = new StringBuffer( value.toString() );
        for ( int i = 0, size = buffer.length(); i < size; i++ )
        {
            switch ( buffer.charAt( i ) )
            {
                case '<':
                    buffer.replace( i, i + 1, LESS_THAN_ENTITY );
                    size += 3;
                    i += 3;
                    break;
                case '>':
                    buffer.replace( i, i + 1, GREATER_THAN_ENTITY );
                    size += 3;
                    i += 3;
                    break;
                case '&':
                    buffer.replace( i, i + 1, AMPERSAND_ENTITY );
                    size += 4;
                    i += 4;
                    break;
                case '\'':
                    buffer.replace( i, i + 1, APOSTROPHE_ENTITY );
                    size += 4;
                    i += 4;
                    break;
                case '\"':
                    buffer.replace( i, i + 1, QUOTE_ENTITY );
                    size += 5;
                    i += 5;
                    break;
            }
        }
        return buffer.toString();
    }
}
