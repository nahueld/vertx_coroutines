
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

class AbstractVerticleServer : AbstractVerticle() {

    //TODO:
    // - add handler in a different function and improve in general
    // - write some tests
    // - add documentation on the project and what we learnt
    // - document questions from showcase
    // - write a blog post ? under the hood
    /**
     * what is the event loop, this guy is the one who controls the execution / updates of handlers
     * why do we need to use in launch
     * different types of coroutine builders
     * how do they make the code cleaner
     * awaitEvent
     * awaitResult
     *
     */

    override fun start() {
        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())

        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET))

        router.get("/doggo/:breed").handler { routingContext ->
            val breed = routingContext.request().getParam("breed")

            val options = HttpClientOptions()
            options.isSsl = true
            options.isTrustAll = true

            val client = vertx.createHttpClient(options)

            client.getNow(443,"dog.ceo" , "/api/breed/$breed/images") { response ->
                when(response.statusCode()) {
                    200 -> {
                        response.bodyHandler { body ->
                            routingContext
                                    .response()
                                    .setStatusCode(response.statusCode())
                                    .end(body)
                        }
                    }
                    else -> {
                        routingContext
                                .response()
                                .setStatusCode(response.statusCode())
                                .end(response.statusMessage())
                    }
                }
            }
        }

        vertx.createHttpServer().requestHandler(router::accept).listen(9000)
    }
}

fun main(args: Array<String>) {
    Vertx.vertx().deployVerticle(AbstractVerticleServer())
}
