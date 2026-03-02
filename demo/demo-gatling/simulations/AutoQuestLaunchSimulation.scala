import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class AutoQuestLaunchSimulation extends Simulation {

  private val MissingQuestId = "__missing_processing_quest__"

  private def requiredEnv(name: String): String =
    sys.env.getOrElse(name, throw new IllegalArgumentException(s"Missing required env var: $name"))

  private def requiredInt(name: String): Int =
    requiredEnv(name).toInt

  val baseUrl: String = requiredEnv("API_BASE_URL")
  val questsBasePath: String = requiredEnv("QUESTS_BASE_PATH")
  val launchDelayMs: Int = requiredInt("LAUNCH_DELAY_MS")
  val users: Int = requiredInt("USERS")
  val durationSeconds: Int = requiredInt("DURATION_SECONDS")
  val thinkTimeMs: Int = requiredInt("THINK_TIME_MS")
  val warmupTimeoutSeconds: Int = requiredInt("WARMUP_TIMEOUT_SECONDS")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val listQuests = http("Lister les quetes AUTO")
    .get(questsBasePath)
    .check(
      status.is(200),
      jsonPath("$[*].id").findAll.optional.saveAs("questIds"),
      jsonPath("$[*].status").findAll.optional.saveAs("questStatuses")
    )

  val assertProcessingQuestAvailable = http("Verifier au moins une quete PROCESSING (AUTO)")
    .get(questsBasePath)
    .check(
      status.is(200),
      jsonPath("$[*].status")
        .findAll
        .transform((statuses: Seq[String]) => statuses.contains("PROCESSING"))
        .is(true)
    )

  val updateAutoReadiness = exec { session =>
    val statuses = session("questStatuses").asOption[Seq[Any]].getOrElse(Seq.empty).map(_.toString)
    val hasProcessingQuest = statuses.contains("PROCESSING")
    session.set("hasProcessingQuest", hasProcessingQuest)
  }

  val waitForAutoProcessing = asLongAsDuring(
    session => !session("hasProcessingQuest").asOption[Boolean].contains(true),
    warmupTimeoutSeconds.seconds
  ) {
    exec(listQuests)
      .exec(updateAutoReadiness)
      .pause(1.second)
  }

  val triggerResolve = http("Declencher resolve AUTO")
    .post(s"$questsBasePath/#{selectedQuestId}/resolve")
    .queryParam("delayMs", launchDelayMs.toString)
    .check(status.is(202))

  val selectProcessingQuest = exec { session =>
    val ids = session("questIds").asOption[Seq[Any]].getOrElse(Seq.empty).map(_.toString)
    val statuses = session("questStatuses").asOption[Seq[Any]].getOrElse(Seq.empty).map(_.toString)
    val processingIndex = statuses.indexOf("PROCESSING")

    if (processingIndex >= 0 && processingIndex < ids.size) {
      session
        .set("selectedQuestId", ids(processingIndex))
        .set("canTriggerResolve", true)
    } else {
      session
        .set("selectedQuestId", MissingQuestId)
        .set("canTriggerResolve", false)
    }
  }

  val scn = scenario("Parcours AUTO - observer et relancer")
    .exec(session => session.set("hasProcessingQuest", false))
    .exec(waitForAutoProcessing)
    .exec(assertProcessingQuestAvailable)
    .during(durationSeconds.seconds) {
      exec(listQuests)
        .exec(updateAutoReadiness)
        .exec(selectProcessingQuest)
        .pause(thinkTimeMs.milliseconds)
        .exec(triggerResolve)
        .pause(1.second)
    }

  setUp(
    scn.inject(
      rampUsers(users) during (durationSeconds.seconds)
    )
  ).protocols(httpProtocol)
}
