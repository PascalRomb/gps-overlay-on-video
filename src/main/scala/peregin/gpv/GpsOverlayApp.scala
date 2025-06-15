package peregin.gpv

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.swing._
import info.BuildInfo
import org.jdesktop.swingx._
import peregin.gpv.gui.TemplatePanel.TemplateEntry
import peregin.gpv.gui._
import peregin.gpv.gui.dashboard.{CyclingDashboard, DashboardPainter}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Logging, Timed}
import peregin.gpv.video._

import scala.swing._
import scala.swing.event.{SelectionChanged, ValueChanged}


object GpsOverlayApp extends SimpleSwingApplication
  with DashboardPainter with VideoPlayer.Listener with TemplatePanel.Listener
  with Logging with Timed {

  log.info("initializing...")

  // alternative to -Xdock:name on MacOs:
  System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GPS Overlay on video")

  Goodies.initLookAndFeel()

  private var projectFile: Option[File] = None
  private var setup = Setup.empty()

  private val videoPanel = new VideoPanel(openVideoData, this) with SeekableVideoPlayerFactory
  private val telemetryPanel = new TelemetryPanel(openGpsData)
  private val statusLabel = new JXLabel("Ready")
  private val transparencySlider = new PercentageSlider
  transparencySlider.orientation = Orientation.Vertical
  transparencySlider.percentage = 80
  private val unitChooser = new ComboBox(Seq("Metric", "Marine","Standard"))
  private val templatePanel = new TemplatePanel(GpsOverlayApp.this)
  val frame = new MainFrame {
    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val toolbar = new JToolBar
      toolbar.add(new ImageButton("images/new.png", "New", newProject()))
      toolbar.add(new ImageButton("images/open.png", "Open", openProject()))
      toolbar.add(new ImageButton("images/save.png", "Save", saveProject()))
      toolbar.addSeparator()
      toolbar.add(new ImageButton("images/video.png", "Convert", convertProject()))
      add(toolbar, "span 2, wrap")

      private val unitPanel = new MigPanel("ins 0 5 0 5", "", "") {
        add(new Label("Units"), "")
        add(unitChooser, "")
      }
      add(unitPanel, "span 2, wrap")

      //add(titled("Video", videoPanel), "pushy, width 60%")
      add(titled("Video", new MigPanel("ins 0, fill", "[fill]", "[fill]") {
        add(new MigPanel("ins 40 0 40 0, fill", "[fill]", "[fill]") {
          add(new JXLabel("Transparency") {
            setTextRotation(3*Math.PI/2)
            setVerticalAlignment(SwingConstants.CENTER)
            setHorizontalAlignment(SwingConstants.CENTER)
            setMaximumSize(new Dimension(20, 100))
          }, "")
          add(transparencySlider, "")
        }, "align left")
        add(videoPanel, "grow, push")
      }), "pushy, width 60%")
      add(titled("Telemetry Data", telemetryPanel), "pushy, width 40%, wrap")

      val gaugePanel = new GaugePanel
      add(titled("Gauges", new ScrollPane(gaugePanel)), "height 30%")
      add(titled("Dashboard templates", templatePanel), "height 30%, wrap")

      val statusPanel = new JXStatusBar
      statusPanel.add(statusLabel)
      add(statusPanel, "pushx, growx")
      val link = new JXHyperlink()
      link.setURI(new URI("www.velocorner.com"))
      add(link, "split, w 150!, align right")
    }
  }

  private val spinnerWrap = Component.wrap(telemetryPanel.spinner)
  listenTo(transparencySlider, telemetryPanel.spinner, unitChooser.selection)
  reactions += {
    case ValueChanged(`transparencySlider`) => videoPanel.fireLastVideoEventIfNotPlaying() // will trigger the dashboard repaint
    case ValueChanged(`spinnerWrap`) => videoPanel.fireLastVideoEventIfNotPlaying() // will trigger the dashboard repaint
    case SelectionChanged(`unitChooser`) =>
      val item = unitChooser.selection.item
      log.info(s"switching units to $item")
      setup.units = item
  }

  frame.title = s"GPS data overlay onto video - built ${BuildInfo.buildTime}"
  frame.iconImage = Io.loadImage("images/video.png")
  frame.size = new Dimension(1500, 1000)
  Goodies.center(frame)
  frame.maximize()

  def top: Frame = frame

  def message(s: String): Unit = statusLabel.setText(s)

  def titled(title: String, c: Component): Component = {
    val panel = new JXTitledPanel(title, c.peer)
    Component.wrap(panel)
  }

  def newProject(): Unit = {
    log.info("new project")
    setup = Setup.empty()
    val tm = Telemetry.empty()
    videoPanel.refresh(setup)
    telemetryPanel.refresh(setup, tm)
    transparencySlider.percentage = setup.transparency
    unitChooser.selection.index = if (setup.units == "Standard") 1 else 0
    message("New project has been created")
  }

  def openProject(): Unit = timed("open project") {
    val chooser = new FileChooser()
    chooser.selectedFile = projectFile.orNull
    chooser.fileFilter = ExtensionFilters.project
    chooser.title = "Open project:"
    if (chooser.showOpenDialog(GpsOverlayApp.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      val path = file.getAbsolutePath
      projectFile = Some(file)
      debug(s"opening $path")
      Goodies.showBusy(frame) {
        Swing.onEDT {
          Goodies.showPopupOnFailure(frame) {
            message("Loading...")
            try {
              setup = Setup.loadFile(path)
              debug(s"setup $setup")
              message("Analyzing telemetry...")
              val telemetry = setup.gpsPath.map(p => Telemetry.load(new File(p)))
              val tm = telemetry.getOrElse(Telemetry.empty())
              tm.setCaptions(setup.captions)
              message("Updating...")
              templatePanel.refresh(setup)
              telemetryPanel.refresh(setup, tm)
              transparencySlider.percentage = setup.transparency
              videoPanel.refresh(setup)
              message(s"Project $path has been loaded")
            }
            catch {
              case ex: Throwable => {
                log.error("Error opening project: file={}", path, ex)
                throw ex;
              }
            }
          }
        }
      }
    }
  }

  def saveProject(): Unit = {
    val chooser = new FileChooser()
    chooser.selectedFile = projectFile.orNull
    chooser.fileFilter = ExtensionFilters.project
    chooser.title = "Save project:"
    if (chooser.showSaveDialog(GpsOverlayApp.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      if (!file.exists() ||
          (file.exists() && Dialog.showConfirmation(frame.contents.head, "Do you want to overwrite the file?", "File already exists", Dialog.Options.YesNo) ==  Dialog.Result.Yes)) {
        saveProject(file)
        message(s"Project file has been saved to ${file.getAbsolutePath}")
        projectFile = Some(file)
      }
    }
  }

  private def saveProject(file: File): Unit = {
    log.debug(s"saving ${file.getAbsolutePath}")
    setup.shift = telemetryPanel.getShift
    setup.transparency = transparencySlider.percentage
    setup.dashboardCode = templatePanel.getSelectedEntry.map(entry => entry.dashboard.getName())
    setup.saveFile(file.getAbsolutePath)
  }

  def convertProject(): Unit = {
    log.debug("convert project")
    setup.shift = telemetryPanel.getShift
    setup.transparency = transparencySlider.percentage
    setup.units = unitChooser.selection.item
    val template = templatePanel.getSelectedEntry.getOrElse(TemplateEntry(new CyclingDashboard {}))
    val dialog = new ConverterDialog(setup, telemetryPanel.telemetry, template, frame)
    Goodies.center(dialog)
    dialog.open()
  }

  def openVideoData(file: File): Unit = {
    setup.videoPath = Some(file.getAbsolutePath)
    videoPanel.refresh(setup)
  }

  def openGpsData(file: File): Unit = {
    setup.gpsPath = Some(file.getAbsolutePath)
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(file)
      Swing.onEDT(telemetryPanel.refresh(setup, telemetry))
    }
  }

  override def seekEvent(percentage: Double): Unit = {}

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage, rotation: Double): Unit = {
    paintGauges(telemetryPanel.telemetry, tsInMillis, image, rotation, telemetryPanel.getShift, transparencySlider.percentage, unitChooser.selection.item)
    Swing.onEDT(telemetryPanel.updateVideoProgress(tsInMillis))
  }

  override def videoStarted(): Unit = {}

  override def videoStopped(): Unit = {}

  override def selected(entry: TemplatePanel.TemplateEntry): Unit = {
    dash = entry.dashboard
    log.info(s"dashboard is ${dash.getName()}")
  }
}
