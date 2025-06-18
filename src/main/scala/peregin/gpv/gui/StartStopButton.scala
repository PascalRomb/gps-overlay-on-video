package peregin.gpv.gui

import java.awt.event.{ActionEvent, ActionListener}
import org.jdesktop.swingx.JXButton
import peregin.gpv.util.Io

import java.awt.{Color, Graphics2D}
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.ImageIcon


class StartStopButton[T](action: => T) extends JXButton {

  private val playIcon = Io.loadIcon("images/buttons/play.png")
  private val stopIcon = Io.loadIcon("images/buttons/stop.png")
  private var playing = false

  stop()
  setContentAreaFilled(false)
  setBorderPainted(false)
  setFocusPainted(false)
  setOpaque(false)

  addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) = action
  })

  def isPlaying = playing

  def play(): Unit = {
    playing = true
    setToolTipText("Pause")
    setIcon(stopIcon)
  }

  def stop(): Unit = {
    playing = false
    setToolTipText("Play")
    setIcon(playIcon)
  }
}
