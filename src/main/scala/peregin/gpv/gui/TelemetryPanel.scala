package peregin.gpv.gui

import java.awt.Image
import java.io.File
import javax.swing.ImageIcon
import org.jdesktop.swingx.mapviewer.DefaultTileFactory
import peregin.gpv.Setup
import peregin.gpv.gui.map.{AltitudePanel, MapPanel, MapQuestTileFactory, MicrosoftTileFactory}
import peregin.gpv.model.{Mode, Telemetry}
import peregin.gpv.util.{Io, Logging, Timed}

import scala.swing._
import scala.swing.event.{ButtonClicked, MouseClicked, SelectionChanged}


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging with Timed {

  var telemetry = Telemetry.empty()

  // file chooser widget
  val fileChooser = new FileChooserPanel("Load GPS data file:", openGpsData, ExtensionFilters.gps)
  add(fileChooser, "pushx, growx, wrap")

  val mapKit = new MapPanel
  private val mapKitWrapper = Component.wrap(mapKit)
  add(mapKit, "span 2,height 70%, growx, wrap")

  val altitude = new AltitudePanel
  add(altitude, "span 2, height 30%, grow, gaptop 10, gapbottom 5.5, wrap")

  val direction = new ComboBox(Seq("Forward", "Backward"))
  val spinner = new DurationSpinner

  //TODO want it at the center
  private val controlPanel = new MigPanel("ins 0 5 0 5", "[center][center][center]", "") {
    add(new Label("Shift"), "")
    add(direction, "")
    add(spinner, "")
  }
  add(controlPanel, "gaptop 5, gapbottom 5")

  listenTo(altitude.mouse.clicks, mapKit)

  reactions += {
    case MouseClicked(`altitude`, pt, _, 1, false) => timed(s"time/elevation for x=${pt.x}") {
      val sonda = altitude.sondaForPoint(pt)
      altitude.refreshPoi(sonda)
      mapKit.refreshPoi(sonda.map(_.location))
    }
    case MouseClicked(`mapKitWrapper`, pt, _, 1, false) => timed(s"geo/map for x=${pt.x}, y=${pt.y}") {
      val gp = mapKit.getMainMap.convertPointToGeoPosition(pt)
      log.info(s"geo location $gp")
      val sonda = telemetry.sondaForPosition(gp)
      altitude.refreshPoi(sonda)
      mapKit.refreshPoi(sonda.map(_.location))
    }
  }

  def refresh(setup: Setup, telemetry: Telemetry): Unit = {
    fileChooser.fileInput.text = setup.gpsPath.getOrElse("")
    this.telemetry = telemetry

    mapKit.refresh(telemetry)
    mapKit.setAddressLocation(telemetry.centerGeoPosition)
    mapKit.refreshPoi(None)
    mapKit.refreshProgress(None)

    altitude.refresh(telemetry)
    altitude.refreshPoi(None)
    altitude.refreshProgress(None)

    spinner.duration = setup.shift.abs
    direction.selection.index = if (setup.shift < 0) 1 else 0
  }

  def getShift: Long = spinner.duration * (if (direction.selection.index == 0) 1 else -1)

  // dispatched by the video controller, invoked from EDT
  def updateVideoProgress(videoTimeInMillis: Long): Unit = {
    val sonda = telemetry.sondaForRelativeTime(videoTimeInMillis + getShift)
    altitude.refreshProgress(sonda)
    mapKit.refreshProgress(sonda.map(_.location))
  }
}
