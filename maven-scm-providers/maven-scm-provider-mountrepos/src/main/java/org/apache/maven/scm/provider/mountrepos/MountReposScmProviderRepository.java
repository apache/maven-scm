package org.apache.maven.scm.provider.mountrepos;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.providers.mountrepos.manifest.MountReposManifest;
import org.apache.maven.scm.providers.mountrepos.manifest.RepoProject;
import org.apache.maven.scm.providers.mountrepos.manifest.RepoProjectRemote;
import org.apache.maven.scm.providers.mountrepos.manifest.RepoRemoteDefault;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

/**
 * ScmProviderRepository sub-class for "repo" tool
 */
public class MountReposScmProviderRepository
    extends ScmProviderRepository
{

    private final File repoManifestFile;

    private final MountReposManifest reposManifest;

    public class MountProjectRepository
    {

        public final String path;

        public final RepoProject repoProject;

        public final ScmRepository scmRepository;

        public final String revision;

        public MountProjectRepository( String path, RepoProject repoProject, ScmRepository scmRepository,
                                       String revision )
        {
            this.repoProject = repoProject;
            this.scmRepository = scmRepository;
            this.path = path;
            this.revision = revision;
        }

        public String getPath()
        {
            return path;
        }

        public RepoProject getRepoProject()
        {
            return repoProject;
        }

        public ScmRepository getScmRepository()
        {
            return scmRepository;
        }

        public String getRevision()
        {
            return revision;
        }

    }

    private Map<String, MountProjectRepository> mountProjects;

    public MountReposScmProviderRepository( File repoManifestFile, MountReposManifest reposManifest,
                                            ScmManager scmManager )
        throws ScmRepositoryException
    {
        this.repoManifestFile = repoManifestFile;
        this.reposManifest = reposManifest;
        this.mountProjects = new LinkedHashMap<String, MountProjectRepository>();
        RepoRemoteDefault projectDefault = reposManifest.getProjectDefault();
        String defaultRemote = ( projectDefault != null ) ? projectDefault.getRemote() : null;
        String defaultRevision = ( projectDefault != null ) ? projectDefault.getRevision() : null;
        Map<String, RepoProjectRemote> remoteByName = toRemoteByName( reposManifest );

        for ( RepoProject repoProject : reposManifest.getProjects() )
        {
            MountProjectRepository mountProject =
                resolveProjectRepo( repoProject, scmManager, defaultRemote, defaultRevision, remoteByName );
            mountProjects.put( repoProject.getName(), mountProject );
        }
    }

    private MountProjectRepository resolveProjectRepo( RepoProject repoProject, ScmManager scmManager,
                                                       String defaultRemote, String defaultRevision,
                                                       Map<String, RepoProjectRemote> remoteByName )
        throws ScmRepositoryException
    {
        String projectName = repoProject.getName();
        String remoteName = defaultIfNull( repoProject.getRemote(), defaultRemote );
        String revision = defaultIfNull( repoProject.getRevision(), defaultRevision );
        RepoProjectRemote remote = remoteByName.get( remoteName );
        if ( remote == null )
        {
            throw new ScmRepositoryException( "remote not found for name '" + remoteName + "', available remotes: "
                + remoteByName.keySet() );
        }
        String fetch = remote.getFetch();
        String scmUrl = "scm:jgit:" + fetch + "/" + projectName + ( ( !projectName.endsWith( ".git" ) ) ? ".git" : "" );
        String path = defaultIfNull( repoProject.getPath(), projectName );

        ScmRepository scmRepository;
        try
        {
            scmRepository = scmManager.makeScmRepository( scmUrl );
        }
        catch ( ScmRepositoryException ex )
        {
            throw ex;
        }
        catch ( NoSuchScmProviderException e )
        {
            throw new ScmRepositoryException( "", e ); // should not occur
        }
        return new MountProjectRepository( path, repoProject, scmRepository, revision );
    }

    protected static Map<String, RepoProjectRemote> toRemoteByName( MountReposManifest reposManifest )
    {
        Map<String, RepoProjectRemote> res = new HashMap<String, RepoProjectRemote>();
        for ( RepoProjectRemote remote : reposManifest.getRemotes() )
        {
            res.put( remote.getName(), remote );
            String alias = remote.getAlias();
            if ( alias != null )
            {
                res.put( alias, remote );
            }
        }
        return res;
    }

    protected static String defaultIfNull( String value, String defaultValue )
    {
        return ( value != null ) ? value : defaultValue;
    }

    public File getRepoManifestFile()
    {
        return repoManifestFile;
    }

    public MountReposManifest getReposManifest()
    {
        return reposManifest;
    }

    public Map<String, MountProjectRepository> getProjectScmProviderRepositories()
    {
        return mountProjects;
    }

    public ScmFileSet subSetForMount( ScmFileSet fileSet, MountProjectRepository mountRepo )
        throws IOException
    {
        final String path = mountRepo.path;
        File mountBaseDir = new File( fileSet.getBasedir(), path );
        String[] includes = StringUtils.split( fileSet.getIncludes(), "," );
        String[] excludes = StringUtils.split( fileSet.getExcludes(), "," );

        // convert && filter for base path
        List<String> mountIncludeList = new ArrayList<>();
        List<String> mountExcludeList = new ArrayList<>();
        for ( String include : includes )
        {
            if ( include.startsWith( path ) )
            {
                include = include.substring( 0, path.length() );
                mountIncludeList.add( include );
            }
            else if ( include.startsWith( "*" ) )
            {
                mountIncludeList.add( include );
            }
        }
        for ( String exclude : excludes )
        {
            if ( exclude.startsWith( path ) )
            {
                exclude = exclude.substring( 0, path.length() );
                mountExcludeList.add( exclude );
            }
            else if ( exclude.startsWith( "*" ) )
            {
                mountExcludeList.add( exclude );
            }
        }

        String mountIncludes = StringUtils.join( mountIncludeList.iterator(), "," );
        String mountExcludes = StringUtils.join( mountExcludeList.iterator(), "," );
        return new ScmFileSet( mountBaseDir, mountIncludes, mountExcludes );
    }

}
