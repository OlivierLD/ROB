# 🌊 Tide Engine
## In Java, ES6, and php (in progress)

<!--
### Java
_**To be published as its own artifact on Maven.**_
> See in the `build.gradle` 
> - `// maven { url "https://raw.githubusercontent.com/OlivierLD/ROB/repository" }`
> - `implementation 'raspberry.on.board:TideEngine:1.0'`
-->

#### Maven repo
The artifacts are - at least for now - published in a github repo, accessed as a Maven repository.
See [here](https://github.com/OlivierLD/ROB/blob/repository/README.md).

### Java implementation

> Several implementations of the data storage (coefficients, tide stations) are
> available. For now, we have
> - XML
> - SQLITE
> - JSON  
>
> The `XML` and `JSON` storages are available as Resources. The `SQLITE` db file is external. 

The XML engine is based on XML data, stored in `xml.zip`. Those data are generated after the
harmonic data files found in the `harmonics` directory.
Re-generating those coefficients is not necessary, but in case you're interested, run
```bash
 $ ../gradlew harmonicsXML
```
The sources of the generator are obviously available.

#### Why are we using XML instead of the raw `txt` file?
The format of the harmonic files is a proprietary format. To be used efficiently, the file has to
be parsed and loaded in memory for the data it contains to be available in a timely manner.

This could be quite demanding for a small machine like the Raspberry Pi (the`Zero` has "only" 512 Mb of RAM), even the generation of the XML files
can be challenging for the Raspberry Pi.

Using a SAX Parser allows the amount of required memory to substantially shrink.
As opposed to a DOM Parser that loads the DOM representation of the document in memory,
the SAX parser scans it until the expected data are found.

> Note about the encoding: The XML Data are `ISO-8859-1` encoded.  
> For an easier access, those elements (mainly the String elements, like station names) are returned as `UTF-8` encoded data by REST APIs.

>> TODO: JSON names are OK, XML ones seem to have problems with special characters (Icelandic & Co).  
>> ```
>> Sites coefficients of [Hornafj%C3%B6r%C3%B0ur%2C%20Iceland] fixed for 2022
>> Water Height in [Hornafjörður, Iceland] at Wed Nov 30 09:47:31 CET 2022 : +1.08 meters
>> ```  

#### Unit Tests
Some basic tests are available in the `test (src/main/test/java)` folder.

### ES6 Implementation
More like experimental for now, but seems to be working OK.  
See instructions [here](./ES6/README.md).

### PHP Implementation
See this [doc](./src/main/php/README.md).

---
