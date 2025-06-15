package peregin.gpv.gui.dashboard

import com.fasterxml.jackson.annotation.{JsonIdentityInfo, ObjectIdGenerators}
import peregin.gpv.gui.TemplatePanel.TemplateEntry
import peregin.gpv.gui.gauge.GaugePainter
import peregin.gpv.util.{Logging, YamlConverter}

import java.io.{File, FileInputStream, InputStream}

//FIXME Note that this class will import all dashboard in the folder.
// we can optimize that in order to load just the file name and then load the dashboard only when click on selection.

object YamlResourceDashboardLoader extends Logging{
  val customDashboardFolderName = "customDashboardTemplates"

  def retrieveAllDynamicDashboards(): Array[TemplateEntry] = {
    createDashboardTemplatesFolderIfNotExists()
    val templates = listAllYamlFilenames()
    templates.map(filename => loadAndConvertToDashboardTemplate(filename))
  }

  private def createDashboardTemplatesFolderIfNotExists(): Unit = {
    val dir = new File(customDashboardFolderName)

    if (!dir.exists()) {
      log.info(s"$customDashboardFolderName folder does not exist, will create it.")
      if(!dir.mkdir()) {
        throw new IllegalArgumentException(s"Failed to create directory '${dir.getPath}'.")
      }
    }
  }

  private def listAllYamlFilenames(): Array[String] = {
    val dir = new File(customDashboardFolderName)
    dir.listFiles((_, name) => name.endsWith(".yml") || name.endsWith(".yaml")).map(el => el.getName)
  }

  private def loadAndConvertToDashboardTemplate(file: String): TemplateEntry = {
    val dashboardFileName = s"$customDashboardFolderName/$file"
    val inputStream: InputStream = new FileInputStream(dashboardFileName)
    try {
      TemplateEntry(convertToDashboard(inputStream))
    } finally {
      inputStream.close();
    }
  }

  private def convertToDashboard(inputStream: InputStream): Dashboard = {
    val resource: DashboardResource = YamlConverter.read[DashboardResource](inputStream)
    new DynamicResourceDashboard(resource.name, resource.gauges.map(gauge => {
      GaugeSetup(
        gauge.x,
        gauge.y,
        gauge.size,
        gauge.width,
        gauge.height,
        getClass.getClassLoader.loadClass(gauge.clazz).getConstructor().newInstance().asInstanceOf[GaugePainter]
      )
    }))
  }

  @JsonIdentityInfo(generator = classOf[ObjectIdGenerators.None])
  case class GaugeResource(name: String, x: Double, y: Double, size: Option[Double], width: Option[Double], height: Option[Double], clazz: String) {

  }

  @JsonIdentityInfo(generator = classOf[ObjectIdGenerators.None])
  case class DashboardResource(code: String, name: String, gauges: Seq[GaugeResource]) {
  }
}
