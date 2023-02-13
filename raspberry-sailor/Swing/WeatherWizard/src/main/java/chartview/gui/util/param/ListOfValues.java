package chartview.gui.util.param;

public class ListOfValues
{
  String currentValue = "";

  public ListOfValues()
  {}
  
  public ListOfValues(String str)
  {
    currentValue = str;
  }

  public void setCurrentValue(String str)
  {
    currentValue = str;
  }

  public String getCurrentValue()
  {
    return currentValue;
  }

  public boolean equals(ListOfValues lov)
  {
    return this.toString().equals(lov.toString());
  }

  public String toString()
  {
    return currentValue;
  }
}

