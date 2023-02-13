package chartview.util;

public class UserExitAction
{
  private int rnk;
  private String label;
  private String action;
  private boolean sync;
  private boolean ack;
  private String tip;

  public UserExitAction()
  {
    super();
  }

  public void setRnk(int rnk)
  {
    this.rnk = rnk;
  }

  public int getRnk()
  {
    return rnk;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getLabel()
  {
    return label;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
  }

  public void setSync(boolean sync)
  {
    this.sync = sync;
  }

  public boolean isSync()
  {
    return sync;
  }

  public void setAck(boolean ack)
  {
    this.ack = ack;
  }

  public boolean isAck()
  {
    return ack;
  }

  public void setTip(String tip)
  {
    this.tip = tip;
  }

  public String getTip()
  {
    return tip;
  }
  
  public String toString()
  { return this.label; }
}
