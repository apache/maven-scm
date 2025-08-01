 ------
 How to define a non Apache Maven hosted implementation
 ------
 Maven Team
 ------
 2013-09-07
 ------

~~ Licensed to the Apache Software Foundation (ASF) under one
~~ or more contributor license agreements.  See the NOTICE file
~~ distributed with this work for additional information
~~ regarding copyright ownership.  The ASF licenses this file
~~ to you under the Apache License, Version 2.0 (the
~~ "License"); you may not use this file except in compliance
~~ with the License.  You may obtain a copy of the License at
~~
~~   http://www.apache.org/licenses/LICENSE-2.0
~~
~~ Unless required by applicable law or agreed to in writing,
~~ software distributed under the License is distributed on an
~~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~~ KIND, either express or implied.  See the License for the
~~ specific language governing permissions and limitations
~~ under the License.

~~ NOTE: For help with the syntax of this file, see:
~~ http://maven.apache.org/doxia/references/apt-format.html

How to define a non Apache Maven hosted implementation?

  Apache Maven supports a lot of implementations, but not all. 
  However, with the following steps you can make Maven use this implementation for all your projects.

* Prepare Maven

  Make the SCM implementation available for Maven by downloading the required jars and add them to Maven.
  If there's a <<<jar-with-dependencies>>> or a <<<shaded jar>>>, then that's the only jar required. 
  Otherwise check the project for all transitive dependencies.  
  
  * Maven 3.x : Add jars to <<<%M2_HOME%/lib/ext/>>>
  
  * Maven 2.x : Add jars to <<<%M2_HOME%/lib/>>>
  
  []

  <<Note:>>Be aware that these jars now end up next to the root classloader of Maven. 
  If you experience unexpected behavior, verify there's no class collision.

* Configure settings.xml

  Since it is very well possible to have multiple versions of Maven next to each other, 
  the preferred <<<settings.xml>>> to adjust is the instance with the additional jars.
  
  <<<%M2_HOME%/conf/settings.xml>>>
  
------------------------------------------
<settings>
  ...
  <profiles>
    <profile>
      <id>scm</id>
      <properties>
        <!-- this example sets the implementation for svn to javasvn -->
        <maven.scm.provider.svn.implementation>javasvn</maven.scm.provider.svn.implementation>
      </properties>
    </profile>    
  </profiles>
  
  <activeProfiles>
    <!-- activate it always or use -Pscm -->
    <activeProfile>scm</activeProfile>
  </activeProfiles>
  
</settings>
------------------------------------------

  From now on when this Maven instance is used, it will always use the specified SCM provider implementation.
