package common;

import akka.actor.AbstractActor;
import common.message.ComparePricesResult;

public class Client extends AbstractActor {

    private static final String serverPath = "/user/server";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    getContext().actorSelection(serverPath).tell(msg, getSelf());
                })
                .match(ComparePricesResult.class, msg -> System.out.println(msg.message))
                .build();
    }


}
