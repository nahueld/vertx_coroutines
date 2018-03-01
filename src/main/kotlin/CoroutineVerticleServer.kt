
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

class CoroutineVerticleServer : CoroutineVerticle() {
    suspend override fun start() {
        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())

        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET))

        router.get("/doggo/:breed").handler({ ctx -> launch(ctx.vertx().dispatcher()) { getBreedRouteHandler(ctx) } })

        vertx.createHttpServer().requestHandler(router::accept).listen(9000)
    }
}

suspend fun getBreedRouteHandler(routingContext: RoutingContext) {

    val options = HttpClientOptions(HttpClientOptions().setSsl(true).setTrustAll(true))

    val client = routingContext.vertx().createHttpClient(options)

    val breedImagesResponse = httpGetImages(client, routingContext.request().getParam("breed"))

    when (breedImagesResponse.statusCode()) {
        200 -> routingContext.response().setStatusCode(breedImagesResponse.statusCode()).end(getBody(breedImagesResponse))
        else -> routingContext.response().setStatusCode(breedImagesResponse.statusCode()).end(breedImagesResponse.statusMessage())
    }
}

suspend fun httpGetImages(client : HttpClient, breed : String) : HttpClientResponse = awaitEvent { h ->
    client.getNow(443,"dog.ceo" , "/api/breed/$breed/images", h)
}

suspend fun getBody(response : HttpClientResponse) : Buffer = awaitEvent { h ->
    response.bodyHandler(h)
}

fun main(args: Array<String>) {
    Vertx.vertx().deployVerticle(CoroutineVerticleServer())
}