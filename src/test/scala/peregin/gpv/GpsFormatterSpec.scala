package peregin.gpv

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import peregin.gpv.format.GpsFormatter

//FIXME this test is locale based using String. Issue with dot and comma.
class GpsFormatterSpec extends Specification with ScalaCheck {

  stopOnFail

  "formatLon" should {
    "positive as East" in {
      GpsFormatter.formatLon(18.0697738) === "E018°04'11.2\""
    }
    "zero with padding" in {
      GpsFormatter.formatLon(0) === "E000°00'00.0\""
    }
    "negative as West" in {
      GpsFormatter.formatLon(-18.0697738) === "W018°04'11.2\""
    }
  }

  "formatLat" should {
    "positive as North" in {
      GpsFormatter.formatLat(49.6930908) === "N049°41'35.1\""
    }
    "zero with padding" in {
      GpsFormatter.formatLat(0) === "N000°00'00.0\""
    }
    "positive as South" in {
      GpsFormatter.formatLat(-49.6930908) === "S049°41'35.1\""
    }
  }
}
