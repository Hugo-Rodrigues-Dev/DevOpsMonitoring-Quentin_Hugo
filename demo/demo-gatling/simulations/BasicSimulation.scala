import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  // On lit l'URL de l'API dans une variable d'environnement
  val baseUrl: String = sys.env.getOrElse("API_BASE_URL", "http://localhost:8080")
  val uri: String = sys.env.getOrElse("API_URI", "/actuator/health")
  val users: Int = sys.env.getOrElse("USERS", "10").toInt
  val durationSeconds: Int = sys.env.getOrElse("DURATION_SECONDS", "30").toInt

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