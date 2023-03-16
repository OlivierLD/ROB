# A Maven repo in git

There is a `repository` branch on the [ROB](https://github.com/OlivierLD/ROB) project will be hosting a Maven repo, used to store artifacts generated from here, and used for the build, or from other projects.

See good article at <https://gist.github.com/fernandezpablo85/03cf8b0cd2e7d8527063>

> Another possibility: <https://repsy.io/>

### Sample command:
Let's say we want to publish the module `common-utils`.  
From `ROB/common-utils` (`master` branch, or whatever branch you work on), install on your local maven repo (generating the jar is good enough, actually):
```
$ ../gradlew clean build publishToMavenLocal
```
> _**Important note**_:  
> In Gradle 7+, you need to have a `publish` section in `build.gradle`, like
> ```
> publishing {
>   publications {
>     maven(MavenPublication) {
>           groupId = 'raspberry.on.board'
>           artifactId = 'common-utils'
>           version = '1.0'
>
>           from components.java
>        }
>    }
> }
> ```
> See <https://docs.gradle.org/current/userguide/publishing_maven.html> for details.
>

This command will push the generated jar-file in your _local_ maven repository, usually under `~/.m2/repository`.  
Then, from this local repository, you can get the required generated artifacts, and copy them in _this_ `repository` branch, so it can be reached as any maven artifact. See in the [Examples](#examples) section how to refer to it.

> _**Note**_: you can check the generated artifacts, in the case above by doing a
> ```
> $ ls -lisa ~/.m2/repository/raspberry/on/board/common-utils/1.0/
> total 1768
> 151704829    0 drwxr-xr-x  6 olivierlediouris  staff     192 Mar 15 07:54 .
> 151704828    0 drwxr-xr-x  4 olivierlediouris  staff     128 Mar 15 07:54 ..
> 151704832 1584 -rw-r--r--  1 olivierlediouris  staff  807014 Mar 15 07:54 common-utils-1.0-all.jar
> 151704830  168 -rw-r--r--  1 olivierlediouris  staff   83126 Mar 15 07:54 common-utils-1.0.jar
> 151704833    8 -rw-r--r--  1 olivierlediouris  staff    2908 Mar 15 07:54 common-utils-1.0.module
> 151704831    8 -rw-r--r--  1 olivierlediouris  staff     970 Mar 15 07:54 common-utils-1.0.pom
> $
> ```

Now, from the root of the `repository` branch (and _**not**_ the one you were in above):
```
$ mvn install:install-file \
      -DgroupId=raspberry.on.board \
      -DartifactId=common-utils \
      -Dversion=1.0 \
      -Dfile=${HOME}/.m2/repository/raspberry/on/board/common-utils/1.0/common-utils-1.0.jar \
      -Dpackaging=jar \
      -DgeneratePom=true \
      -DlocalRepositoryPath=.  \
      -DcreateChecksum=true
```
or also
```
GROUP=raspberry.on.board
ARTIFACT=http-tiny-server
VERSION=1.0
$ mvn install:install-file \
      -DgroupId=${GROUP} \
      -DartifactId=${ARTIFACT} \
      -Dversion=${VERSION} \
      -Dfile=${HOME}/.m2/repository/raspberry/on/board/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar \
      -Dpackaging=jar \
      -DgeneratePom=true \
      -DlocalRepositoryPath=. \
      -DcreateChecksum=true
```
**_Shortcut available!_**: The script `push.sh` will help you with the steps above, prompting you for the required data.

_Then_, `git add <whatever-you-added>`, `git commit`, and `git push` on the `repository` branch.
> _Note_: _**Do make sure**_ you've added and committed the jar files!! Use `git add -f` if needed.  
> When adding the files (`git add`), you might want to use the `-f` flag to force the jars in.

The repo URL will be: <https://raw.githubusercontent.com/OlivierLD/ROB/repository>   

### Examples
- From Maven
```xml
<!-- https://raw.githubusercontent.com/OlivierLD/ROB/repository -->
<dependency>
    <groupId>raspberry.on.board</groupId>
    <artifactId>common-utils</artifactId>
    <version>1.0</version>
</dependency>
```

- From Gradle
```groovygit
repositories {
    mavenLocal()
    mavenCentral()
    . . .
    maven { url "https://raw.githubusercontent.com/OlivierLD/ROB/repository" }   // Maven on GIT
}

dependencies {
    . . .
    implementation 'raspberry.on.board:common-utils:1.0'
    . . .
}    
```
In both cases (Maven, or Gradle), you need to add the Maven repository URL (you'll figure out how to do it, you can do it):
```
"https://raw.githubusercontent.com/OlivierLD/ROB/repository"
```
The artifacts above will be refered to by a URL like <https://raw.githubusercontent.com/OlivierLD/ROB/repository/raspberry/on/board/common-utils/1.0/common-utils-1.0.pom>


> **_Note_, for <span style="color: red;">Java</span>**
> - Make sure the artifacts are compiled with the right Java version before committing and pushing them, some Raspberry Pi Zero (and early Raspberry Pis, in general) might not like Java above version 8...

---
