package chartview.gui.util.dialog;

import java.awt.Color;

public class FaxType implements Comparable<FaxType>
{
  String value;
  Color color = Color.black;
  boolean show = true;
  boolean transparent = true;
  boolean changeColor = true; // Change black to this.color
  int rank;
  double rotation = 0D;
  String comment = "";
  String origin = "";
  String title = "";

  public FaxType(String v, Color c, boolean b, boolean tr, double d, String orig, String t, boolean cc)
  {
    value = v;
    color = c;
    show = b;
    transparent = tr;
    rotation = d;
    origin = orig;
    title = t;
    changeColor = cc;
  }
  public FaxType(String v, Color c, boolean b, boolean tr, double d, String orig, String t)
  {
    value = v;
    color = c;
    show = b;
    transparent = tr;
    rotation = d;
    origin = orig;
    title = t;
    changeColor = (color != null);
  }
  
  public void setChangeColor(boolean b)
  {
    this.changeColor = b;
  }
  
  public boolean isChangeColor()
  {
    return changeColor;
  }

  public String toString()
  {
    return (comment.length()>0?comment:value);
  }

  public String getValue()
  {
    return value;
  }

  public Color getColor()
  {
    return color;
  }

  public void setValue(String s)
  {
    value = s;
  }

  public void setColor(Color c)
  {
    color = c;
  }

  public int compareTo(FaxType ft)
  {
    return (this.rank - ft.getRank());
  }

  public void setRank(int rank)
  {
    this.rank = rank;
  }

  public int getRank()
  {
    return rank;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public String getComment()
  {
    return comment;
  }

  public void setShow(boolean show)
  {
    this.show = show;
  }

  public boolean isShow()
  {
    return show;
  }

  public void setTransparent(boolean transparent)
  {
    this.transparent = transparent;
  }

  public boolean isTransparent()
  {
    return transparent;
  }

  public void setRotation(double rotation)
  {
    this.rotation = rotation;
  }

  public double getRotation()
  {
    return rotation;
  }

  public void setOrigin(String origin)
  {
    this.origin = origin;
  }

  public String getOrigin()
  {
    return origin;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return title;
  }
}
