package org.apache.maven.scm.provider.hg;

import static org.junit.Assert.*;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Test;

public class HgUtilsTest
{

    @Test
    public void testNullWorkingDirectory()
        throws Exception
    {
        Commandline cmd = HgUtils.buildCmd( null, new String[] {} );
        assertEquals( null, cmd.getWorkingDirectory() );
    }
}