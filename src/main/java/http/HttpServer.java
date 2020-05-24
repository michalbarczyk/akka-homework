package http;

import akka.NotUsed;

import static akka.http.javadsl.server.PathMatchers.integerSegment;
import static akka.pattern.Patterns.ask;
import akka.http.javadsl.marshallers.jackson.Jackson;
import static java.util.regex.Pattern.compile;
import static akka.http.javadsl.server.PathMatchers.segment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import common.PriceComparator;
import common.Server;
import common.message.ComparePricesRequest;
import common.message.ComparePricesResult;


import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class HttpServer extends AllDirectives {

    private final Http http;
    private final ActorSystem system;
    private final ActorRef server;
    private final ActorMaterializer materializer;
    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow;
    final CompletionStage<ServerBinding> binding;

    private static final String serverPath = "/user/server";

    public HttpServer(ActorSystem system, ActorRef server) {
        this.server = server;
        this.system = system;
        http = Http.get(system);
        materializer = ActorMaterializer.create(system);
        routeFlow = this.createRoute().flow(system, materializer);
        binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8060), materializer);
    }

    private Route createRoute() {
        return route(path(PathMatchers.segment("price")
                        .slash(PathMatchers.segment()), product -> {
                            Duration timeout = Duration.ofSeconds(5);
                            ActorRef comparator = system.actorOf(Props.create(PriceComparator.class));
                            CompletionStage<ComparePricesResult> price = ask(comparator, new ComparePricesRequest(product), timeout)
                                    .thenApply(ComparePricesResult.class::cast);
                            return completeOKWithFuture(price, Jackson.marshaller());
                }
        ));
    }
}
