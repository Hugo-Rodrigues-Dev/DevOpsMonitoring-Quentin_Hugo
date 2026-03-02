import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  private def requiredEnv(name: String): String =
    sys.env.getOrElse(name, throw new IllegalArgumentException(s"Missing required env var: $name"))

  private def requiredInt(name: String): Int =
    requiredEnv(name).toInt

  val baseUrl: String = requiredEnv("API_BASE_URL")
  val uri: String = requiredEnv("API_URI")
  val users: Int = requiredInt("USERS")
  val durationSeconds: Int = requiredInt("DURATION_SECONDS")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")

  val scn = scenario("Simple API Bench")
    .exec(
      http("Call HTTP")
        .get(uri) // à adapter selon ton API
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      rampUsers(users) during (durationSeconds.seconds)
    )
  ).protocols(httpProtocol)
}
