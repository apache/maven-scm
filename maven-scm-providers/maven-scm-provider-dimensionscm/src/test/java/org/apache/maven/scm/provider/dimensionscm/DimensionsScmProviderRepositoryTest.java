package org.apache.maven.scm.provider.dimensionscm;

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

import org.apache.maven.scm.provider.dimensionscm.repository.DimensionsScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DimensionsScmProviderRepositoryTest
{

    private static final String url1 = "//dmsys:dmsys_test@vmsql2k8win32:671/cm_typical@dim10/qlarius:test";
    private static final String url2 = "//dmsys:dmsys_test@vmsql2k8win32/cm_typical@dim10/qlarius:test/";
    private static final String url3 = "//dmsys:dmsys_test@vmsql2k8win32:871/cm_typical@dim10/qlarius:test/documents";
    private static final String url4 = "//dmsys:dmsys_test@vmsql2k8win32/cm_typical@dim10/qlarius:test/documents/files";


    @Test
    public void testGetUser() throws ScmRepositoryException
    {
        assertEquals( "dmsys", new DimensionsScmProviderRepository( url1 ).getDmUser() );
        assertEquals( "dmsys", new DimensionsScmProviderRepository( url2 ).getDmUser() );
        assertEquals( "dmsys", new DimensionsScmProviderRepository( url3 ).getDmUser() );
        assertEquals( "dmsys", new DimensionsScmProviderRepository( url4 ).getDmUser() );
    }

    @Test
    public void testGetPassword() throws ScmRepositoryException
    {
        assertEquals( "dmsys_test", new DimensionsScmProviderRepository( url1 ).getDmPassword() );
        assertEquals( "dmsys_test", new DimensionsScmProviderRepository( url2 ).getDmPassword() );
        assertEquals( "dmsys_test", new DimensionsScmProviderRepository( url3 ).getDmPassword() );
        assertEquals( "dmsys_test", new DimensionsScmProviderRepository( url4 ).getDmPassword() );
    }

    @Test
    public void testGetDmServer() throws ScmRepositoryException 
    {
        assertEquals( "vmsql2k8win32", new DimensionsScmProviderRepository( url1 ).getDmServer() );
        assertEquals( "vmsql2k8win32", new DimensionsScmProviderRepository( url2 ).getDmServer() );
        assertEquals( "vmsql2k8win32", new DimensionsScmProviderRepository( url3 ).getDmServer() );
        assertEquals( "vmsql2k8win32", new DimensionsScmProviderRepository( url4 ).getDmServer() );
    }

    @Test
    public void testGetDmDatabaseName() throws ScmRepositoryException
    {
        assertEquals( "cm_typical", new DimensionsScmProviderRepository( url1 ).getDmDatabaseName() );
        assertEquals( "cm_typical", new DimensionsScmProviderRepository( url2 ).getDmDatabaseName() );
        assertEquals( "cm_typical", new DimensionsScmProviderRepository( url3 ).getDmDatabaseName() );
        assertEquals( "cm_typical", new DimensionsScmProviderRepository( url4 ).getDmDatabaseName() );
    }

    @Test
    public void testGetDmDatabaseConnection() throws ScmRepositoryException
    {
        assertEquals( "dim10", new DimensionsScmProviderRepository( url1 ).getDmDatabaseConnection() );
        assertEquals( "dim10", new DimensionsScmProviderRepository( url2 ).getDmDatabaseConnection() );
        assertEquals( "dim10", new DimensionsScmProviderRepository( url3 ).getDmDatabaseConnection() );
        assertEquals( "dim10", new DimensionsScmProviderRepository( url4 ).getDmDatabaseConnection() );
    }

    @Test
    public void testGetDmDatabase() throws ScmRepositoryException
    {
        assertEquals( "cm_typical@dim10", new DimensionsScmProviderRepository( url1 ).getDmDatabase() );
        assertEquals( "cm_typical@dim10", new DimensionsScmProviderRepository( url2 ).getDmDatabase() );
        assertEquals( "cm_typical@dim10", new DimensionsScmProviderRepository( url3 ).getDmDatabase() );
        assertEquals( "cm_typical@dim10", new DimensionsScmProviderRepository( url4 ).getDmDatabase() );
    }

    @Test
    public void testGetDmPort() throws ScmRepositoryException
    {
        assertEquals( "671", new DimensionsScmProviderRepository( url1 ).getDmPort() );
        assertEquals( "671", new DimensionsScmProviderRepository( url2 ).getDmPort() );
        assertEquals( "871", new DimensionsScmProviderRepository( url3 ).getDmPort() );
        assertEquals( "671", new DimensionsScmProviderRepository( url4 ).getDmPort() );
    }

    @Test
    public void testGetDmProject() throws ScmRepositoryException
    {
        assertEquals( "test", new DimensionsScmProviderRepository( url1 ).getDmProject() );
        assertEquals( "test", new DimensionsScmProviderRepository( url2 ).getDmProject() );
        assertEquals( "test", new DimensionsScmProviderRepository( url3 ).getDmProject() );
        assertEquals( "test", new DimensionsScmProviderRepository( url4 ).getDmProject() );
    }

    @Test
    public void testGetDmProduct() throws ScmRepositoryException
    {
        assertEquals( "qlarius", new DimensionsScmProviderRepository( url1 ).getDmProduct() );
        assertEquals( "qlarius", new DimensionsScmProviderRepository( url2 ).getDmProduct() );
        assertEquals( "qlarius", new DimensionsScmProviderRepository( url3 ).getDmProduct() );
        assertEquals( "qlarius", new DimensionsScmProviderRepository( url4 ).getDmProduct() );
    }

    @Test
    public void testDmProjectSpec() throws ScmRepositoryException
    {
        assertEquals( "qlarius:test", new DimensionsScmProviderRepository( url1 ).getDmProjectSpec() );
        assertEquals( "qlarius:test", new DimensionsScmProviderRepository( url2 ).getDmProjectSpec() );
        assertEquals( "qlarius:test", new DimensionsScmProviderRepository( url3 ).getDmProjectSpec() );
        assertEquals( "qlarius:test", new DimensionsScmProviderRepository( url4 ).getDmProjectSpec() );
    }

    @Test
    public void testDmRelativeLocation() throws ScmRepositoryException
    {
        assertEquals( "", new DimensionsScmProviderRepository( url1 ).getDmDirectoryPath() );
        assertEquals( "", new DimensionsScmProviderRepository (url2 ).getDmDirectoryPath() );
        assertEquals( "documents", new DimensionsScmProviderRepository( url3 ).getDmDirectoryPath() );
        assertEquals( "documents/files", new DimensionsScmProviderRepository( url4 ).getDmDirectoryPath() );
    }

}
