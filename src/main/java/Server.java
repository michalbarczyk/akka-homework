import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import message.ComparePricesRequest;

public class Server extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {

                    ActorRef priceComparator = context().actorOf(Props.create(PriceComparator.class));
                    priceComparator.tell(new ComparePricesRequest(sender(), msg), self());
                })
                .build();
    }
}