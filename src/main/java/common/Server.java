package common;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import common.message.ComparePricesRequest;
import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.pipe;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Server extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {

                    ActorRef priceComparator = context().actorOf(Props.create(PriceComparator.class));

                    CompletableFuture<Object> fut = ask(priceComparator, new ComparePricesRequest(msg), Duration.ofSeconds(5))
                            .toCompletableFuture();

                    pipe(fut, getContext().dispatcher()).to(getSender());
                })
                .build();
    }
}