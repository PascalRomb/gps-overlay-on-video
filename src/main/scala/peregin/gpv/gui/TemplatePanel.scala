package peregin.gpv.gui

import java.awt.{Color, Component, Font, Graphics, Graphics2D}
import javax.swing._
import javax.swing.event.ListSelectionEvent
import org.jdesktop.swingx.JXList
import peregin.gpv.Setup
import peregin.gpv.gui.TemplatePanel.{Listener, TemplateEntry}
import peregin.gpv.gui.dashboard.{Dashboard, YamlDashboardLoader}
import peregin.gpv.gui.gauge.{ElevationChart}
import peregin.gpv.model.{InputValue, MinMax, Sonda, Telemetry}
import peregin.gpv.util.Io

import scala.jdk.CollectionConverters._


//TODO remove it.
object TemplatePanel {

  //TODO do we need it?
  case class TemplateEntry(dashboard: Dashboard) {
    override def toString: String = dashboard.getName()
  }

  trait Listener {
    def selected(entry: TemplateEntry): Unit
  }

}

// save/load/use dashboard templates (set of already selected and aligned gauges)
class TemplatePanel(listener: Listener) extends MigPanel("ins 2", "[fill]", "[fill]") {

  class TemplateCellRenderer extends JLabel with ListCellRenderer[TemplateEntry] {

    val anIcon: Icon = Io.loadIcon("images/video.png") //TODO change that

    setOpaque(true)

    override def getListCellRendererComponent(list: JList[_ <: TemplateEntry], value: TemplateEntry, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component = {

      if (isSelected) {
        setBackground(list.getSelectionBackground)
        setForeground(list.getSelectionForeground)
      } else {
        setBackground(list.getBackground)
        setForeground(list.getForeground)
      }

      setFont(list.getFont)
      setText(value.toString)

      setIcon(anIcon)
      this
    }
  }

  //TODO make it scrollable
  val model = new DefaultListModel[TemplateEntry]
  model.addAll(YamlDashboardLoader.retrieveAllDefaultDashboards().toSeq.asJavaCollection)
  model.addAll(YamlDashboardLoader.retrieveAllCustomDashboards().toSeq.asJavaCollection)

  val templates = new JXList(model)
  templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  //templates.setSelectedIndex(0)
  templates.setFont(new Font("Arial", Font.BOLD, 18))
  templates.setCellRenderer(new TemplateCellRenderer)

  add(templates, "grow, push")

  def getSelectedEntry: Option[TemplateEntry] = templates.getSelectedValue match {
    case entry: TemplateEntry => Some(entry)
    case _ => None
  }

  templates.addListSelectionListener((e: ListSelectionEvent) => {
    if (!e.getValueIsAdjusting) {
      getSelectedEntry.foreach { entry =>
        listener.selected(entry)
        preview.repaint()
      }
    }
  })

  //TODO remove that
  // shows the current selection
  val preview = new JPanel() {

    // clone painters
    private val name2Dashboard = model.elements().asScala.map {
      entry =>
        val dashboard = entry.dashboard.clone()
        // setup with default values
        dashboard.gauges().foreach {
          case e: ElevationChart => e.telemetry = Telemetry.sample()
          case _ =>
        }
        (dashboard.getName(), dashboard)
    }.toMap

    override def paint(g: Graphics): Unit = {
      val width = getWidth
      val height = getHeight
      g.setColor(Color.black)
      g.fillRect(0, 0, width, height)

      getSelectedEntry.flatMap(e => name2Dashboard.get(e.dashboard.getName())).foreach { d =>
        d.paintDashboard(g.asInstanceOf[Graphics2D], width, height, width / 5, Sonda.sample())
      }
    }
  }

  add(preview, "grow, push")

  def refresh(setup: Setup): Unit = {
    if (setup.dashboardCode.isDefined) {
      for (i <- 0 until model.size()) {
        if (model.get(i).dashboard.getName().equals(setup.dashboardCode.get)) {
          templates.setSelectedIndex(i)
          return
        }
      }
    }
  }

}
