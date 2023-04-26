## GRIB Reader, and Routing (in progress)
Exposes Routing features through REST.  
See how it can be put to work in `RESTNavServer`.


The GRIB part is assumed by `JGRIB` (sibling of this module, copied from [JGRIB](https://jgrib.sourceforge.net/))

The server generates a json document, as in `poc.GRIBBulk`, representing the GRIB file's data.
The rendering is (to be) done on the client (HTML5/CSS/ES6) over a map. See in `RESTNavServer/web`.

And later, add the faxes.

Run `./runGRIBserver.sh`

Then reach <http://localhost:1234/web/index.html> :

![Test API](./screenshot.00.png)  <!-- TODO replace this screenshot -->

### TODO
- GRIB Request generator ?
- Support for GRIB v2.
- Other grib providers than `saildocs`. 
  - See <https://opengribs.org/en/gribs> 
  - See <https://www.zygrib.org/>
  - See <http://ma-meteo-marine.com/fr/>
