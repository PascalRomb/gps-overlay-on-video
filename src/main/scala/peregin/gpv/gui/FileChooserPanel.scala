package peregin.gpv.gui

import java.io.File
import peregin.gpv.util.Logging
import scala.swing._
import scala.swing.event.ButtonClicked
import peregin.gpv.GpsOverlayApp
import javax.swing.filechooser.FileNameExtensionFilter


class FileChooserPanel(info: String, action: File => Unit, filter: FileNameExtensionFilter, openDialog: Boolean = true) extends MigPanel("ins 0", "", "[grow, fill]") with Logging {
  add(new Label(info), "span 2, wrap")
  val browseButton = new Button("Browse")
  add(browseButton, "")
  val fileInput = new TextArea("")
  fileInput.lineWrap = false
  fileInput.wordWrap  = false
  fileInput.peer.setCaretPosition(0)
  val scrollPane: ScrollPane = new ScrollPane(fileInput) {
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Never
  }

  add(scrollPane, "growx, pushx, wrap")

  listenTo(browseButton)
  reactions += {
    case ButtonClicked(`browseButton`) => openData
  }

  def openData = {
    val file = fileInput.text.trim
    val dir = if (file.isEmpty) null else new File(file).getParentFile
    val chooser = new FileChooser(dir)
    chooser.fileFilter = filter
    chooser.title = info
    val openFunc: (Component) => FileChooser.Result.Value = if (openDialog) chooser.showOpenDialog else chooser.showSaveDialog
    if (openFunc(GpsOverlayApp.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"opening ${file.getAbsolutePath}")
      action(file)
    }
  }
}
