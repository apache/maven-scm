package org.apache.maven.scm;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
     * Formatter used by the getDateFormatted method.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );

    /**
     * Formatter used by the getTimeFormatted method.
     */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat( "HH:mm:ss" );

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

    public ChangeSet( String date, String comment, String author, List files )
    {
        setDate( date );

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
        return (Date) date.clone();
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
        try
        {
            this.date = TIMESTAMP_FORMAT_1.parse( date );
        }
        catch ( ParseException e )
        {
            try
            {
                this.date = TIMESTAMP_FORMAT_2.parse( date );
            }
            catch ( ParseException ex )
            {
                throw new IllegalArgumentException( "Unable to parse CVS date: " + date );
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
}
