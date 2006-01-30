package org.apache.maven.scm.provider.bazaar.command.checkout;

import org.apache.maven.scm.provider.bazaar.BazaarTestUtils;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarCheckOutCommandTest
    extends CheckOutCommandTckTest
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
