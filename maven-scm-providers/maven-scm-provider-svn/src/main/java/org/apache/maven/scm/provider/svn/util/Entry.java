package org.apache.maven.scm.provider.svn.util;

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

/**
 * Class Entry.
 *
 * @version $Revision$ $Date$
 */
public class Entry
    implements java.io.Serializable
{

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field committedRevision
     */
    private int committedRevision = 0;

    /**
     * Field name
     */
    private String name;

    /**
     * Field committedDate
     */
    private String committedDate;

    /**
     * Field url
     */
    private String url;

    /**
     * Field lastAuthor
     */
    private String lastAuthor;

    /**
     * Field kind
     */
    private String kind;

    /**
     * Field uuid
     */
    private String uuid;

    /**
     * Field propertyTime
     */
    private String propertyTime;

    /**
     * Field revision
     */
    private int revision = 0;

    /**
     * Field textTime
     */
    private String textTime;

    /**
     * Field checksum
     */
    private String checksum;

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method getChecksum
     */
    public String getChecksum()
    {
        return this.checksum;
    } //-- String getChecksum() 

    /**
     * Method getCommittedDate
     */
    public String getCommittedDate()
    {
        return this.committedDate;
    } //-- String getCommittedDate() 

    /**
     * Method getCommittedRevision
     */
    public int getCommittedRevision()
    {
        return this.committedRevision;
    } //-- int getCommittedRevision() 

    /**
     * Method getKind
     */
    public String getKind()
    {
        return this.kind;
    } //-- String getKind() 

    /**
     * Method getLastAuthor
     */
    public String getLastAuthor()
    {
        return this.lastAuthor;
    } //-- String getLastAuthor() 

    /**
     * Method getName
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Method getPropertyTime
     */
    public String getPropertyTime()
    {
        return this.propertyTime;
    } //-- String getPropertyTime() 

    /**
     * Method getRevision
     */
    public int getRevision()
    {
        return this.revision;
    } //-- int getRevision() 

    /**
     * Method getTextTime
     */
    public String getTextTime()
    {
        return this.textTime;
    } //-- String getTextTime() 

    /**
     * Method getUrl
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Method getUuid
     */
    public String getUuid()
    {
        return this.uuid;
    } //-- String getUuid() 

    /**
     * Method setChecksum
     *
     * @param checksum
     */
    public void setChecksum( String checksum )
    {
        this.checksum = checksum;
    } //-- void setChecksum(String) 

    /**
     * Method setCommittedDate
     *
     * @param committedDate
     */
    public void setCommittedDate( String committedDate )
    {
        this.committedDate = committedDate;
    } //-- void setCommittedDate(String) 

    /**
     * Method setCommittedRevision
     *
     * @param committedRevision
     */
    public void setCommittedRevision( int committedRevision )
    {
        this.committedRevision = committedRevision;
    } //-- void setCommittedRevision(int) 

    /**
     * Method setKind
     *
     * @param kind
     */
    public void setKind( String kind )
    {
        this.kind = kind;
    } //-- void setKind(String) 

    /**
     * Method setLastAuthor
     *
     * @param lastAuthor
     */
    public void setLastAuthor( String lastAuthor )
    {
        this.lastAuthor = lastAuthor;
    } //-- void setLastAuthor(String) 

    /**
     * Method setName
     *
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Method setPropertyTime
     *
     * @param propertyTime
     */
    public void setPropertyTime( String propertyTime )
    {
        this.propertyTime = propertyTime;
    } //-- void setPropertyTime(String) 

    /**
     * Method setRevision
     *
     * @param revision
     */
    public void setRevision( int revision )
    {
        this.revision = revision;
    } //-- void setRevision(int) 

    /**
     * Method setTextTime
     *
     * @param textTime
     */
    public void setTextTime( String textTime )
    {
        this.textTime = textTime;
    } //-- void setTextTime(String) 

    /**
     * Method setUrl
     *
     * @param url
     */
    public void setUrl( String url )
    {
        this.url = url;
    } //-- void setUrl(String) 

    /**
     * Method setUuid
     *
     * @param uuid
     */
    public void setUuid( String uuid )
    {
        this.uuid = uuid;
    } //-- void setUuid(String) 

}
