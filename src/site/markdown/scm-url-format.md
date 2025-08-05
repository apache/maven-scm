---
title: SCM URL format
author: 
  - Emmanuel Venisse
date: 2005-06-05
---

<!-- Licensed to the Apache Software Foundation (ASF) under one-->
<!-- or more contributor license agreements.  See the NOTICE file-->
<!-- distributed with this work for additional information-->
<!-- regarding copyright ownership.  The ASF licenses this file-->
<!-- to you under the Apache License, Version 2.0 (the-->
<!-- "License"); you may not use this file except in compliance-->
<!-- with the License.  You may obtain a copy of the License at-->
<!---->
<!--   http://www.apache.org/licenses/LICENSE-2.0-->
<!---->
<!-- Unless required by applicable law or agreed to in writing,-->
<!-- software distributed under the License is distributed on an-->
<!-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY-->
<!-- KIND, either express or implied.  See the License for the-->
<!-- specific language governing permissions and limitations-->
<!-- under the License.-->
# SCM URL Format

## SCM URL

The general format for a SCM URL is

```
scm:<provider id><delimiter><provider-specific part>
```

As delimiter you can use either colon `:` or a pipe `|`, if you use a colon for one of the variables \(e.g. a Windows path\).

For information about supported provider IDs and the provider-specific part, see the appropriate [SCM implementation](./scms-overview.html).

Provider IDs may be remapped with the help of the plugin parameter `providerImplementations` supported by both [maven-release-plugin](https://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#providerImplementations) and [maven-scm-plugin](./maven-scm-plugin/checkout-mojo.html#providerImplementations).

