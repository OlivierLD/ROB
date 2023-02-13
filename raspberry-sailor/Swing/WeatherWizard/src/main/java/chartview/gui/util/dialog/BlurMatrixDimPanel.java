package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class BlurMatrixDimPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel matrixLabel = new JLabel();
  private transient SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
  private JSpinner matrixSpinner = new JSpinner(spinnerModel);

  @SuppressWarnings("compatibility:-5951699133653371924")
  private final static long serialVersionUID = 1L;
  
  public BlurMatrixDimPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(252, 186));
    matrixLabel.setText(WWGnlUtilities.buildMessage("matrix-dimension"));
    this.add(matrixLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 5), 0, 0));
    this.add(matrixSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public void setMatrixSize(int i)
  {
    matrixSpinner.setValue(i);
  }
  
  public int getMatrixSize()
  {
    return ((Integer)matrixSpinner.getValue()).intValue();
  }
}
