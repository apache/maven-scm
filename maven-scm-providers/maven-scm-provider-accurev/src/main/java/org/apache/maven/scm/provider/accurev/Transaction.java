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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class Transaction
{

    public class Version
    {

        private String realSpec;

        private String virtualSpec;

        private String ancestorSpec;

        private Long elementId;

        private String elementName;

        private Version( Long id, String elementName, String virtualSpec, String realSpec, String ancestor )
        {
            this.elementId = id;
            this.virtualSpec = virtualSpec;
            this.realSpec = realSpec;
            this.ancestorSpec = ancestor;
            this.elementName = elementName;
        }

        public String getVirtualSpec()
        {
            return virtualSpec;
        }

        public void setVirtualSpec( String virtualSpec )
        {
            this.virtualSpec = virtualSpec;
        }

        public String getAncestorSpec()
        {
            return ancestorSpec;
        }

        public void setAncestorSpec( String ancestorSpec )
        {
            this.ancestorSpec = ancestorSpec;
        }

        public void setRealSpec( String realSpec )
        {
            this.realSpec = realSpec;
        }

        public void setElementId( Long elementId )
        {
            this.elementId = elementId;
        }

        public String getRealSpec()
        {
            return realSpec;
        }

        public Long getElementId()
        {
            return elementId;
        }

        public Transaction getTransaction()
        {
            return getOuterTransaction();
        }

        public String getElementName()
        {
            return elementName;
        }

    }

    private Collection<Version> versions = new HashSet<Version>();

    private long id;

    public Transaction( Long id, Date when, String tranType, String user )
    {
        this.id = id;
        this.tranType = tranType;
        this.when = when;
        this.author = user;
    }

    public long getId()
    {
        return id;
    }

    public String getTranType()
    {
        return tranType;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    private Date when;

    private String tranType;

    private String author;

    private String comment;

    public long getTranId()
    {
        return id;
    }

    private Transaction getOuterTransaction()
    {
        return this;
    }

    public Collection<Version> getVersions()
    {
        return versions;
    }

    public Date getWhen()
    {
        return when;
    }

    public String getType()
    {
        return tranType;
    }

    public String getAuthor()
    {
        return author;
    }

    public void addVersion( Long id, String name, String virtualSpec, String realSpec, String ancestor )
    {
        Transaction.Version v = new Version( id, name, virtualSpec, realSpec, ancestor );
        versions.add( v );

    }

}
