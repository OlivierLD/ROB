package chartview.gui.util.dialog.places;

public class Position 
{
  Latitude L;
  Longitude G;
  String gpsMnemo = "";
  
  public Position(Latitude L, Longitude G)
  {
    this(L, G, "");
  }
  public Position(Latitude L, Longitude G, String gps)
  {
    this.L = L;
    this.G = G;
    this.gpsMnemo = gps;
  }
  public Position(String fmtString)
  {
    String[] sa = fmtString.split("/");
    try
    {
      Latitude lat = new Latitude(sa[0].trim());
      Longitude lng = new Longitude(sa[1].trim());
      this.L = lat;
      this.G = lng;
      this.gpsMnemo = "";
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  public Latitude getLat()
  { return L; }
  public Longitude getLong()
  { return G; }
  public String getGpsMnemo()
  { return gpsMnemo; }

  public String toString()
  {
    return this.L.toString() + "/" + this.G.toString();
  }
}