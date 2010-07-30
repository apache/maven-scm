package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Date;

public class Stream
{

    private String name;

    private long id;

    private String basis;

    private long basisId;

    private String depot;

    private Date startDate;

    private String streamType;

    public Stream( String name, long id, String basis, long basisId, String depot, Date startDate, String streamType )
    {
        this.name = name;
        this.id = id;
        this.basis = basis;
        this.basisId = basisId;
        this.depot = depot;
        this.startDate = startDate;
        this.streamType = streamType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( basis == null ) ? 0 : basis.hashCode() );
        result = prime * result + (int) ( basisId ^ ( basisId >>> 32 ) );
        result = prime * result + ( ( depot == null ) ? 0 : depot.hashCode() );
        result = prime * result + (int) ( id ^ ( id >>> 32 ) );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( startDate == null ) ? 0 : startDate.hashCode() );
        result = prime * result + ( ( streamType == null ) ? 0 : streamType.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Stream other = (Stream) obj;
        if ( basis == null )
        {
            if ( other.basis != null )
                return false;
        }
        else if ( !basis.equals( other.basis ) )
            return false;
        if ( basisId != other.basisId )
            return false;
        if ( depot == null )
        {
            if ( other.depot != null )
                return false;
        }
        else if ( !depot.equals( other.depot ) )
            return false;
        if ( id != other.id )
            return false;
        if ( name == null )
        {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals( other.name ) )
            return false;
        if ( startDate == null )
        {
            if ( other.startDate != null )
                return false;
        }
        else if ( !startDate.equals( other.startDate ) )
            return false;
        if ( streamType == null )
        {
            if ( other.streamType != null )
                return false;
        }
        else if ( !streamType.equals( other.streamType ) )
            return false;
        return true;
    }

    public String getName()
    {
        return name;
    }

    public long getId()
    {
        return id;
    }

    public String getBasis()
    {
        return basis;
    }

    public long getBasisId()
    {
        return basisId;
    }

    public String getDepot()
    {
        return depot;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public String getStreamType()
    {
        return streamType;
    }

    public Boolean isWorkspace()
    {
        return "workspace".equals( streamType );
    }

}
