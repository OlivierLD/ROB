# Weather Wizard

Runs with the Metal Look and Feel.  
To keep the system Look and Feel, use `-Dkeep.system.lnf=true` at runtime.
## WiP. Being ported.

This project depends on several other projects. 

You can whether:
- build it for yourself, based on the latest source files (build it from [here](https://github.com/OlivierLD/oliv-soft-project-builder/)).
- <strike>get a pre-built version, from the [this page](https://drive.google.com/open?id=0B1OXF1qWHj9mZFZxUkV1cUZFblk&authuser=0) on Google Drive</strike>.

Latest version is currently 4.0.0.0.

Written in Java. As such, runs everywhere Java runs (Windows, Linux, Mac OS, etc)

It has been written because I was unable to find anything equivalent anywhere.
Paperback User Manual available at [Lulu.com](http://www.lulu.com/shop/olivier-le-diouris/weather-wizard-user-manual/paperback/product-20064234.html).

There is [here](http://donpedro.lediouris.net/weather/applet/chartapplet.html) a signed applet, demonstrating <i>some</i> of the features of the product.

Please report unwanted behaviors, or address enhancement requests, to [me](mailto:olivier.lediouris@gmail.com olivier.lediouris@gmail.com).

## What it does, what it does not
This is *not* a GRIB viewer, *nor* a fax viewer. Such utilities already exist elsewhere.

The idea at the origin of this project is that one:

On a chart, with a given projection (*Mercator, Lambert, globe, satellite views, square, polar stereographic, and more*) you can *_superimpose_* several weather faxes (like surface analysis, 500mb analysis, wave height, sat pictures, etc) and GRIB Data (wind speed & direction, 500mb, PRMSL, etc).
The tool allows you to do this, store, and restore.

What it does with each fax to display:
- It makes it transparent (so other faxes in the same area can be seen as well). _<font size="-3">This is optional, for example it is not necessary for satellite pictures, which will be displayed as they are, at the very bottom layer.</font>_
- It changes the color. _<font size="-3">Optional as well, same as above</font>._
- It rotates it if necessary
- It resizes it. _<font size="-3">You can even change the width/height ratio</font>._
- It puts it on the chart, at a specific location, based on projections, scales and offsets
Boat position, heading and track can also be added on the chart.

Each component can be hidden or shown on demand, simply by unchecking or checking a box.

Even fuzzy faxes (like the ones you sometimes receive at sea) can make sense.

Interface with Google Map and Google Earth.

Also implements some routing capabilities. A tool to elaborate your polars comes along with the project.
<p align="center">
  <img src="http://weather.lediouris.net/wizard/01.png" width="610" height="430" alt="WeatherWizard"/>
  <br/>
  Three faxes (surface, 500mb, streamlines) plus a GRIB. The rendering above can be obtained in *one* click.
<br/>
In short, the goal of the soft is to be able to view on the <i>same</i> document:
<ul>
  <li type="disc">A chart</li>
  <li type="disc">Zero or one GRIB files</li>
  <li type="disc">Zero or more fax(es)</li>
  <li type="disc">If wanted, the boat position, and its track (from the GPS, or manually entered)</li>
</ul>
And to simplify your life - specially when sailing - all this is as automated as possible. You can recall an already used configuration in about one click (like display the same faxes and GRIBs as yesterday, but with today's data).

More details and screenshots are available on the [Weather Wizard site](http://weather.lediouris.net).

Contact [me](mailto:olivier.lediouris@gmail.com?cc=olivier@lediouris.net&subject=Weather+Wizard) for any further question.
