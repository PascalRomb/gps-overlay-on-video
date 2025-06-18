package peregin.gpv.gui

import org.jdesktop.swingx.JXButton
import java.awt.event.{ActionEvent, ActionListener}
import peregin.gpv.util.Io


class SkipFrameButton[T](action: => T) extends JXButton {
  setToolTipText("Skip frame")
  setContentAreaFilled(false)
  setBorderPainted(false)
  setFocusPainted(false)
  setOpaque(false)

  setIcon(Io.loadIcon("images/buttons/skip_forward.png"))

  addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) = action
  })
}
