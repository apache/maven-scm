package org.apache.maven.scm.provider.accurev.cli;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.provider.accurev.AccuRevStat;
import org.junit.Test;

public class AccuRevStatTest
{

    private static final String[] STAT_RESULTS;

    static
    {
        String defunct = "./defunct.xml            maventst_ggardner/3 (2/3) (defunct) (kept) (member)";
        String modified = "./src/main/java/Modified.java  maventst_ggardner/2 (2/2) (modified) (member)";
        String kept = "./src/main/java/Kept.java  maventst_ggardner/3 (2/3) (kept) (member)";
        String external = "./src/test/java/External.java  (external)";
        String missing = "./Missing.jpg            maventst/1 (2/1) (missing)";

        STAT_RESULTS = new String[] { defunct, modified, kept, external, missing };
    }

    @Test
    public void testDEFUNCT()
        throws Exception
    {
        assertStatType( AccuRevStat.DEFUNCT, "-D", "./defunct.xml" );
    }

    @Test
    public void testEXTERNAL()
        throws Exception
    {
        assertStatType( AccuRevStat.EXTERNAL, "-x", "./src/test/java/External.java" );
    }

    @Test
    public void testKEPT()
        throws Exception
    {
        assertStatType( AccuRevStat.KEPT, "-k", "./src/main/java/Kept.java", "./defunct.xml" );
    }

    @Test
    public void testMISSING()
        throws Exception
    {
        assertStatType( AccuRevStat.MISSING, "-M", "./Missing.jpg" );
    }

    @Test
    public void testMODIFIED()
        throws Exception
    {
        assertStatType( AccuRevStat.MODIFIED, "-m", "./src/main/java/Modified.java" );
    }

    private void assertStatType( AccuRevStat accuRevStat, String expectedStatArg, String... expectedMatches )
    {
        assertThat( accuRevStat.getStatArg(), is( expectedStatArg ) );
        Pattern matchPattern = accuRevStat.getMatchPattern();

        int matchCount = 0;

        for ( String stat : STAT_RESULTS )
        {

            Matcher matcher = matchPattern.matcher( stat );

            if ( matcher.matches() )
            {
                String matched = matcher.group( 1 );
                assertThat( matched, isOneOf( expectedMatches ) );
                matchCount++;
            }
        }
        assertThat( matchCount, is( expectedMatches.length ) );
    }
}
