package peregin.gpv.gui.map

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.jdesktop.swingx.painter.Painter
import org.jdesktop.swingx.{JXMapKit, JXMapViewer}
import peregin.gpv.model.Telemetry

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.{Color, Color => _, Component => _, Dimension => _, Graphics2D => _, _}
import javax.swing.{Box, BoxLayout, JButton, JPanel}
import scala.swing._
import scala.swing.event.MouseClicked

class MapPanel extends JXMapKit with Publisher with KnobPainter {

  private var telemetry = Telemetry.empty()
  private var poi: Option[GeoPosition] = None
  private var progress: Option[GeoPosition] = None

  customizeZoomButtonsLayout()

  setDefaultProvider(JXMapKit.DefaultProviders.Custom)
  setTileFactory(new MicrosoftTileFactory)
  setDataProviderCreditShown(true)
  setMiniMapVisible(false)
  setAddressLocation(telemetry.centerGeoPosition)
  setZoom(6)

  getMainMap.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      publish(MouseClicked(Component.wrap(MapPanel.this), e.getPoint, e.getModifiers, e.getClickCount, e.isPopupTrigger)(e))
    }
  })

  val routePainter = new Painter[JXMapViewer] {
    override def paint(g2: Graphics2D, `object`: JXMapViewer, width: Int, height: Int) = {
      val g = g2.create().asInstanceOf[Graphics2D]
      // convert from viewport to world bitmap
      val rect = getMainMap.getViewportBounds
      g.translate(-rect.x, -rect.y)

      // do the drawing
      g.setColor(Color.RED)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setStroke(new BasicStroke(2))

      var lastX = -1
      var lastY = -1
      val region = telemetry.track.map(_.position)
      region.foreach{ gp =>
        // convert geo to world bitmap pixel
        val pt = getMainMap.getTileFactory.geoToPixel(gp, getMainMap.getZoom)
        if (lastX != -1 && lastY != -1) {
          g.drawLine(lastX, lastY, pt.getX.toInt, pt.getY.toInt)
        }
        lastX = pt.getX.toInt
        lastY = pt.getY.toInt
      }

      poi.foreach(gp => paintKnob(g, getMainMap.getTileFactory.geoToPixel(gp, getMainMap.getZoom), Color.blue))
      progress.foreach(gp => paintKnob(g, getMainMap.getTileFactory.geoToPixel(gp, getMainMap.getZoom), Color.orange))

      g.dispose()
    }
  }
  getMainMap.setOverlayPainter(routePainter)

  def refresh(telemetry: Telemetry): Unit = {
    this.telemetry = telemetry
  }

  def refreshPoi(sonda: Option[GeoPosition]): Unit = {
    poi = sonda
    repaint()
  }

  def refreshProgress(sonda: Option[GeoPosition]): Unit = {
    progress = sonda
    repaint()
  }


  private def customizeZoomButtonsLayout(): Unit = {
    setZoomSliderVisible(false)

    val zoomInBtn = getZoomInButton
    val zoomOutBtn = getZoomOutButton

    customizeZoomButton(zoomInBtn, "+")
    customizeZoomButton(zoomOutBtn, "–")

    remove(zoomInBtn)
    remove(zoomOutBtn)

    val zoomPanel = new JPanel()
    zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.Y_AXIS))
    zoomPanel.setOpaque(false)
    zoomPanel.setBorder(null)
    zoomPanel.add(zoomInBtn)
    zoomPanel.add(Box.createVerticalStrut(5))
    zoomPanel.add(zoomOutBtn)

    getMainMap.setLayout(null) // overlay layout
    getMainMap.add(zoomPanel)

    zoomPanel.setBounds(0, 0, 30, 90)

    getMainMap.addComponentListener(new java.awt.event.ComponentAdapter {
      override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
        val panelWidth = 30
        val x = getMainMap.getWidth - panelWidth - 10
        val y = getMainMap.getHeight - zoomPanel.getPreferredSize.height - 10
        zoomPanel.setBounds(x, y, panelWidth, zoomPanel.getPreferredSize.height)
      }
    })

  }

  private def customizeZoomButton(btn: JButton, text: String): Unit = {
    val sizeBtn = new Dimension(30, 30)
    btn.setPreferredSize(sizeBtn)
    btn.setMinimumSize(sizeBtn)
    btn.setMaximumSize(sizeBtn)
    btn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20))
    btn.setOpaque(false)
    btn.setBorderPainted(false)
    btn.setIcon(null)
    btn.setText(text)
    btn.setForeground( new java.awt.Color(160, 160, 160))
  }
}
