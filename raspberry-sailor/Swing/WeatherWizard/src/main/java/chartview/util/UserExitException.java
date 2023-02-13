package chartview.util;

@SuppressWarnings("serial")
public class UserExitException
  extends Exception
{
  public UserExitException(Throwable throwable)
  {
    super(throwable);
  }

  public UserExitException(String string, Throwable throwable)
  {
    super(string, throwable);
  }

  public UserExitException(String string)
  {
    super(string);
  }

  public UserExitException()
  {
    super();
  }
}
