import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * ChainedApiSimulation — Enchaînement de trois appels avec extraction JSON.
 *
 * Scénario :
 *   1. GET /posts          → extrait l'id et le userId du premier post
 *                            → session["firstPostId"], session["firstUserId"]
 *   2. GET /posts/{id}     → extrait le titre du post
 *                            → session["postTitle"]
 *   3. GET /users/{userId} → vérifie que l'auteur existe (HTTP 200)
 *
 * API de test par défaut : https://jsonplaceholder.typicode.com
 * Remplacer API_BASE_URL pour cibler votre propre API.
 *
 * Usage Docker :
 *   API_BASE_URL=https://jsonplaceholder.typicode.com \
 *   GATLING_SIMULATION_CLASS=ChainedApiSimulation \
 *   docker compose up --build
 */
class ChainedApiSimulation extends Simulation {

  val baseUrl: String      = sys.env.getOrElse("API_BASE_URL", "https://jsonplaceholder.typicode.com")
  val users: Int           = sys.env.getOrElse("USERS", "5").toInt
  val durationSeconds: Int = sys.env.getOrElse("DURATION_SECONDS", "30").toInt

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // ---------------------------------------------------------------------------
  // Étape 1 — Récupérer la liste des posts
  //   On extrait l'id et le userId du premier élément du tableau JSON.
  //   La réponse est un tableau JSON : [{"id": 1, "userId": 1, ...}, ...]
  // ---------------------------------------------------------------------------
  val getPostsList = http("1 - GET /posts")
    .get("/posts")
    .check(
      status.is(200),
      jsonPath("$[0].id").saveAs("firstPostId"),       // stocké en session
      jsonPath("$[0].userId").saveAs("firstUserId")    // stocké en session
    )

  // ---------------------------------------------------------------------------
  // Étape 2 — Récupérer le détail du post
  //   On utilise #{firstPostId} depuis la session.
  //   On extrait le titre pour illustrer une extraction en profondeur.
  // ---------------------------------------------------------------------------
  val getPostDetail = http("2 - GET /posts/#{firstPostId}")
    .get("/posts/#{firstPostId}")
    .check(
      status.is(200),
      jsonPath("$.title").saveAs("postTitle")          // stocké en session
    )

  // ---------------------------------------------------------------------------
  // Étape 3 — Récupérer l'auteur du post
  //   On utilise #{firstUserId} extrait à l'étape 1.
  // ---------------------------------------------------------------------------
  val getUser = http("3 - GET /users/#{firstUserId} (auteur de '#{postTitle}')")
    .get("/users/#{firstUserId}")
    .check(status.is(200))

  // ---------------------------------------------------------------------------
  // Scénario — enchaîne les trois étapes avec une pause réaliste
  // ---------------------------------------------------------------------------
  val scn = scenario("Chained API — Post → Detail → Author")
    .exec(getPostsList)
    .pause(500.milliseconds)    // pause entre les appels (comportement réaliste)
    .exec(getPostDetail)
    .pause(500.milliseconds)
    .exec(getUser)

  setUp(
    scn.inject(
      rampUsers(users) during (durationSeconds.seconds)
    )
  ).protocols(httpProtocol)
}
