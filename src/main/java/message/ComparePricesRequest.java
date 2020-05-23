package message;

import akka.actor.ActorRef;

public class ComparePricesRequest {
    public final ActorRef sender;
    public final String product;
    public ComparePricesRequest(ActorRef sender, String product) {
        this.sender = sender;
        this.product = product;
    }
}
