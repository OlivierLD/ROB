package chartview.util;

import chartview.ctx.WWContext;

import chartview.gui.right.CommandPanel;

import java.util.ArrayList;
import java.util.List;

public interface UserExitInterface
{
  public boolean isAvailable(CommandPanel cp, WWContext ctx);
  public boolean userExitTask(CommandPanel cp, WWContext ctx) throws UserExitException;
  public List<String> getFeedback();
}
