import akka.actor.AbstractActor;
import message.GetPrice;

import java.util.concurrent.ThreadLocalRandom;

public class Shop extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetPrice.class, msg -> sender().tell(checkPrice(), self()))
                .build();
    }

    private Integer checkPrice() {

        long sleepTime = ThreadLocalRandom.current().nextLong(100, 501);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ThreadLocalRandom.current().nextInt(1, 11);
    }
}
