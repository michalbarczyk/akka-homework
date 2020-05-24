package common.message;

import akka.actor.ActorRef;

public class ComparePricesRequest {
    public final String product;
    public ComparePricesRequest(String product) {
        this.product = product;
    }
}
