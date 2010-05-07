package org.apache.maven.scm.provider.accurev.command;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.cli.AccuRevCommandLine;
import org.apache.maven.scm.provider.accurev.cli.AccuRevJUnitUtil;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.StringUtils;

public class AccuRevTckUtil
{

    private String depotName = null;

    private AccuRevCommandLine accurevCL;

    private AccuRevInfo info;

    private String url;

    private ScmLogger logger;

    private String tckBaseDir;

    public static String getSystemProperty( String name, String defaultValue )
    {
        String mavenProperty = "${" + name + "}";
        String result = System.getProperty( name, mavenProperty );
        if ( mavenProperty.equals( result ) )
        {
            result = defaultValue;
        }
        return result;
    }

    public String getScmUrl()
        throws Exception
    {
        if ( url == null )
        {

            // Either tckUrlPrefix or tckAllowImpliedConnection must be set.
            // This is to prevent accidentally running the tck tests against say your production accurev server

            String tckUrlPrefix = getSystemProperty( "tckUrlPrefix", "" );

            if ( StringUtils.isBlank( tckUrlPrefix ) )
            {
                assertThat( "Property \"tckUrlPrefix\" is not set."
                    + " To enable tck tests against an externally logged in accurev session,"
                    + " please set property \"tckAllowImpliedLogin\" to \"true\"",
                            getSystemProperty( "tckAllowImpliedLogin", "false" ), is( "true" ) );
            }
            else
            {
                assertThat( "tckUrlPrefix must of the form [[user[/pass]]@host[:port]", tckUrlPrefix,
                            containsString( "@" ) );
            }

            url = "scm:accurev:" + tckUrlPrefix + ":" + getDepotName();

            getLogger().debug( "Using scmURL=" + url );
        }

        return url;

    }

    private void setLogger( PlexusContainer plexusContainer )
        throws Exception
    {
        this.logger = AccuRevJUnitUtil.getLogger( plexusContainer );
    }

    public void initRepo( PlexusContainer container )
        throws Exception
    {
        setLogger( container );
        initRepo();
    }

    @SuppressWarnings("unchecked")
    private void initRepo()
        throws Exception
    {

        assertLoggedInOK();

        assertThat( "Can't execute TckTests in an accurev workspace, please set 'tckBaseDir' property",
                    getAccuRevInfo().isWorkSpace(), is( false ) );

        File initDir = ScmTestCase.getTestFile( getTckBaseDir(), "target/" + getDepotName() + "/init" );

        assertThat( "AccuRev workspace path limit of 127 characters execeeded, please set 'basedir' property", initDir
            .getAbsolutePath().length(), lessThan( 127 ) );

        getAccuRevCL().mkdepot( getDepotName() );

        /*
         * Since scmFileNames is not populated before this is called... we get to duplicate some
         * code here.TODO raise patch to fix this.
         */

        List<String> scmFileNames = new ArrayList( 4 );
        scmFileNames.add( "/pom.xml" );
        scmFileNames.add( "/readme.txt" );
        scmFileNames.add( "/src/main/java/Application.java" );
        scmFileNames.add( "/src/test/java/Test.java" );

        for ( String filename : scmFileNames )
        {
            ScmTestCase.makeFile( initDir, filename, filename );
        }

        String initWorkSpace = getDepotName() + "_initRepo";
        getAccuRevCL().mkws( getDepotName(), initWorkSpace, initDir );

        getAccuRevCL().add( initDir, null, "initial version", new ArrayList<File>() );
        getAccuRevCL().promoteAll( initDir, "initial version", new ArrayList<File>() );

        getAccuRevCL().rmws( initWorkSpace + "_" + getAccuRevInfo().getUser() );
    }

    private String getTckBaseDir()
    {
        if ( tckBaseDir == null )
        {
            tckBaseDir = getSystemProperty( "tckBaseDir", "" );
            if ( StringUtils.isBlank( tckBaseDir ) )
            {
                tckBaseDir = ScmTestCase.getBasedir();
            }
            getLogger().debug( "tckBaseDir=" + tckBaseDir );

        }

        return tckBaseDir;
    }

    private void assertLoggedInOK()
        throws Exception
    {

        assertThat( getAccuRevInfo().getUser(), notNullValue() );
        assertThat( getAccuRevInfo().getUser(), is( not( "(not logged in)" ) ) );
    }

    public void tearDown()
        throws Exception
    {
        // nothing left...
    }

    public String getDepotName()
    {
        if ( depotName == null )
        {
            depotName = "mvnscm_" + ( System.currentTimeMillis() / 1000 );
        }
        return depotName;
    }

    public ScmLogger getLogger()
    {
        if ( logger == null )
        {
            logger = new DefaultLog();
        }

        return logger;
    }

    public AccuRevCommandLine getAccuRevCL()
        throws Exception
    {
        if ( accurevCL == null )
        {
            AccuRevScmProvider provider = new AccuRevScmProvider();
            provider.addListener( getLogger() );
            AccuRevScmProviderRepository repo = (AccuRevScmProviderRepository) provider
                .makeProviderScmRepository( getScmUrl(), ':' );
            getLogger().debug( repo.toString() );
            accurevCL = (AccuRevCommandLine) repo.getAccuRev();

            if ( !StringUtils.isEmpty( repo.getUser() ) )
            {
                accurevCL.login( repo.getUser(), repo.getPassword() );
            }

        }

        return accurevCL;
    }

    public void removeWorkSpace( File basedir )
        throws Exception
    {
        try
        {
            assertLoggedInOK();
        }
        catch ( AssertionError e )
        {
            return;
        }
        if ( basedir.exists() )
        {
            AccuRevInfo bdInfo = accurevCL.info( basedir );
            if ( bdInfo.isWorkSpaceTop() )
            {
                accurevCL.promoteAll( basedir, "clear default group", new ArrayList<File>() );
                accurevCL.rmws( bdInfo.getWorkSpace() );
            }
        }
    }

    public AccuRevInfo getAccuRevInfo()
        throws Exception
    {
        if ( info == null )
        {
            File basedir = new File( getTckBaseDir() );
            info = getAccuRevCL().info( basedir );
        }

        return info;

    }

    /*
     * Need to put this in a sub directory because you can't re-use workspace directories And for
     * some stupid reason we only have 127 characters available for the path name
     */
    public File getWorkingCopy()
    {
        return ScmTestCase.getTestFile( getTckBaseDir(), "target/" + getDepotName() + "/co" );
    }

    public File getAssertionCopy()
    {
        return ScmTestCase.getTestFile( getTckBaseDir(), "target/" + getDepotName() + "/as" );
    }

    public File getUpdatingCopy()
    {
        return ScmTestCase.getTestFile( getTckBaseDir(), "target/" + getDepotName() + "/up" );
    }
}
