# Composite Catalog
The goal is to have a catalog definition, usable from JavaScript as well as from Java (REST Service).  
The first version is `catalog.js`, its content is obviously accessible from JavaScript, and can be used from Java through the `ScriptEngine`s, 
available since Java 8, specially `Nashorn`.  

But `Nashorn` is scheduled to be removed after JDK 11... So we moved to a `json` definition, that can be loaded from JavaScript through a `fetch`, and from Java using Jackson.  
One drawback is the consistency check. Colors, Effects, and Projections were referred to as JSON objects in the `js` version, but this is not possible in the `json` version. User has to make sure the values are correct inb the `json` definition of the catalog.

---
The `catalog.js` is here just for illustration and reference, it is not used in the code anymore.

---
