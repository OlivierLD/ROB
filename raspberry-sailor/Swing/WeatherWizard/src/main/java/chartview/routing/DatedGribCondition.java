package chartview.routing;

import chartview.util.grib.GribHelper;

import java.util.Date;

public class DatedGribCondition extends GribHelper.GribCondition
{
  Date date = null;
  
  public DatedGribCondition()
  {
    super();
  }

  public DatedGribCondition(GribHelper.GribCondition gc)
  {
    super();    
    this.windspeed = gc.windspeed;
    this.winddir   = gc.winddir;
    this.hgt500    = gc.hgt500;
    this.horIdx    = gc.horIdx;
    this.vertIdx   = gc.vertIdx;
    this.prmsl     = gc.prmsl;
    this.waves     = gc.waves;
    this.temp      = gc.temp;
    this.rain      = gc.rain;
    this.comment   = gc.comment;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public Date getDate()
  {
    return date;
  }
}
