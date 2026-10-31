---
title: SCM Implementation: Subversion
author: 
  - Wim Deblauwe
  - Olivier Lamy
date: 2011-05-15
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
# SCM Implementation: Subversion

## General Info

Link: <https://subversion.apache.org/>

License: Apache License, Version 2\.0

## SCM URL

The syntax of Subversion SCM URLs is described in [SCM URLs](https://maven.apache.org/pom-scm-urls.html#Subversion).

## Provider Configuration

The provider configuration is defined in `${user.home}/.scm/svn-settings.xml`. For more information see the [reference guide](./maven-scm-providers/maven-scm-providers-svn/maven-scm-provider-svn-commons/svn-settings.html).

### Configuration directory

You can define the subversion configuration directory (&apos;--config-dir&apos; svn global option) in the provider configuration file or with &apos;maven.scm.svn.config_directory&apos; command line parameter.

```
mvn -Dmaven.scm.svn.config_directory=your_configuration_directory scm:update
```

