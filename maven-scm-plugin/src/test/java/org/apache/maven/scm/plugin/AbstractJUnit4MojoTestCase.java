package org.apache.maven.scm.plugin;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.ResolverExpressionEvaluatorStub;
import org.apache.maven.scm.PlexusJUnit4TestSupport;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.logging.LoggerManager;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.Map;

public abstract class AbstractJUnit4MojoTestCase extends AbstractMojoTestCase
{
    private static final PlexusJUnit4TestSupport plexusJUnit4TestSupport = new PlexusJUnit4TestSupport();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        plexusJUnit4TestSupport.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        plexusJUnit4TestSupport.tearDown();
    }

    public static String getBasedir()
    {
        return PlexusJUnit4TestSupport.getBasedir();
    }

    public static File getTestFile(final String path)
    {
        return PlexusJUnit4TestSupport.getTestFile( getBasedir(), path );
    }
}
