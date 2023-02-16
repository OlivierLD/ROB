package chartview.util;

import chartview.ctx.WWContext;
import chartview.gui.right.CommandPanel;

import java.util.List;

public interface UserExitInterface {
    boolean isAvailable(CommandPanel cp, WWContext ctx);
    boolean userExitTask(CommandPanel cp, WWContext ctx) throws UserExitException;
    List<String> getFeedback();
}
