import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class IhmQuestLaunchSimulation extends Simulation {

  val baseUrl: String = sys.env.getOrElse("API_BASE_URL", "http://royaume-app:8080")
  val users: Int = sys.env.getOrElse("USERS", "20").toInt
  val durationSeconds: Int = sys.env.getOrElse("DURATION_SECONDS", "60").toInt
  val thinkTimeMs: Int = sys.env.getOrElse("THINK_TIME_MS", "750").toInt

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val listQuests = http("Lister les quetes IHM")
    .get("/api/royaume/quests")
    .check(
      status.is(200),
      jsonPath("$[*].id").findAll.optional.saveAs("questIds"),
      jsonPath("$[*].latitude").findAll.optional.saveAs("questLatitudes"),
      jsonPath("$[*].longitude").findAll.optional.saveAs("questLongitudes")
    )

  val launchQuest = http("Lancer une quete IHM")
    .post("/api/royaume/quests/#{selectedQuestId}/launch")
    .queryParam("delayMs", "0")
    .check(status.is(202))

  val selectRandomQuest = exec { session =>
    val ids = session("questIds").asOption[Seq[Any]].getOrElse(Seq.empty).map(_.toString)

    if (ids.isEmpty) {
      session
        .remove("selectedQuestId")
        .set("hasQuestToLaunch", false)
    } else {
      val selectedIndex = Random.nextInt(ids.size)
      val selectedId = ids(selectedIndex)
      val latitudes = session("questLatitudes").asOption[Seq[Any]].getOrElse(Seq.empty)
      val longitudes = session("questLongitudes").asOption[Seq[Any]].getOrElse(Seq.empty)

      val selectedLatitude = if (selectedIndex < latitudes.size) latitudes(selectedIndex).toString else "n/a"
      val selectedLongitude = if (selectedIndex < longitudes.size) longitudes(selectedIndex).toString else "n/a"
      val zoomLevel = 4 + Random.nextInt(7)

      session
        .set("selectedQuestId", selectedId)
        .set("selectedLatitude", selectedLatitude)
        .set("selectedLongitude", selectedLongitude)
        .set("zoomLevel", zoomLevel)
        .set("hasQuestToLaunch", true)
    }
  }

  val scn = scenario("Parcours IHM - lister et lancer une quete")
    .during(durationSeconds.seconds) {
      exec(listQuests)
        .exec(selectRandomQuest)
        .pause(thinkTimeMs.milliseconds)
        .doIf(session => session("hasQuestToLaunch").asOption[Boolean].contains(true)) {
          exec(launchQuest)
        }
        .pause(1.second)
    }

  setUp(
    scn.inject(
      rampUsers(users) during (durationSeconds.seconds)
    )
  ).protocols(httpProtocol)
}
