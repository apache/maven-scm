package org.apache.maven.scm.provider.bazaar.command.update;

import org.apache.maven.scm.provider.bazaar.BazaarTestUtils;
import org.apache.maven.scm.tck.command.update.UpdateCommandTckTest;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarUpdateCommandTest
    extends UpdateCommandTckTest
{
    public String getScmUrl()
        throws Exception
    {
        return BazaarTestUtils.getScmUrl();
    }

    public void initRepo()
        throws Exception
    {
        BazaarTestUtils.initRepo();
    }
}
