package peregin.gpv.gui

import peregin.gpv.Setup
import peregin.gpv.gui.TemplatePanel.{Listener, TemplateEntry}
import peregin.gpv.gui.dashboard.{Dashboard, YamlDashboardLoader}

import javax.swing._

object TemplatePanel {
  case class TemplateEntry(dashboard: Dashboard) {
    override def toString: String = dashboard.getName()
  }

  trait Listener {
    def selected(entry: TemplateEntry): Unit
  }

}

// save/load/use dashboard templates (set of already selected and aligned gauges)
class TemplatePanel(listener: Listener) extends MigPanel("ins 2", "[fill]", "[fill]") {

  val templates: Seq[TemplateEntry] =
    YamlDashboardLoader.retrieveAllDefaultDashboards().toSeq ++
      YamlDashboardLoader.retrieveAllCustomDashboards().toSeq

  val comboModel = new DefaultComboBoxModel[TemplateEntry](templates.toArray)

  val templatesCombo = new JComboBox[TemplateEntry](comboModel)
  listener.selected(getSelectedEntry.orNull)
  add(templatesCombo, "growx, pushx")


  def getSelectedEntry: Option[TemplateEntry] = templatesCombo.getSelectedItem match {
    case entry: TemplateEntry => Some(entry)
    case _ => None
  }
  templatesCombo.addActionListener { _ =>
    if (getSelectedEntry.isDefined) {
      listener.selected(getSelectedEntry.get)
    }
  }

  def refresh(setup: Setup): Unit = {
    if (setup.dashboardCode.isDefined) {
      comboModel.setSelectedItem()
      for (i <- 0 until comboModel.getSize) {
        val element = comboModel.getElementAt(i)
        if (element.dashboard.getName().equals(setup.dashboardCode.get)) {
          comboModel.setSelectedItem(element)
          return
        }
      }
    }
  }

}
