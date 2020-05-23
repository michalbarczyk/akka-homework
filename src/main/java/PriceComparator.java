import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import message.ComparePricesRequest;
import message.ComparePricesResult;
import message.GetPrice;
import message.UpdateDbRequest;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class PriceComparator extends AbstractActor {

    private Duration timeout = Duration.ofMillis(300);
    private Duration dbTimeout = Duration.ofMillis(1000);
    private Integer resultPrice = null;
    private Integer noOfQuestions = 0;

    private Integer noOfCompleted = 0;

    private ActorRef requestSender;
    private ActorRef shop1;
    private ActorRef shop2;
    private ActorRef dbHelper;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ComparePricesRequest.class, msg -> {

                    this.requestSender = msg.sender;

                    this.shop1 = context().actorOf(Props.create(Shop.class));
                    this.shop2 = context().actorOf(Props.create(Shop.class));
                    this.dbHelper = context().actorOf(Props.create(DbHelper.class));

                    CompletableFuture<Object> futurePrice1 = ask(shop1, new GetPrice(), timeout).toCompletableFuture();
                    CompletableFuture<Object> futurePrice2 = ask(shop2, new GetPrice(), timeout).toCompletableFuture();
                    CompletableFuture<Object> futureNoOfQuestions = ask(dbHelper, new UpdateDbRequest(msg.product), dbTimeout).toCompletableFuture();

                    futurePrice1.whenComplete((res, err) -> {
                        handlePriceCompleted((Integer) res);
                    });
                    futurePrice2.whenComplete((res, err) -> {
                        handlePriceCompleted((Integer) res);
                    });
                    futureNoOfQuestions.whenComplete((res, err) -> {
                        handleDbUpdated((Integer) res);
                    });

                })
                .build();
    }

    private void handlePriceCompleted(Integer price) {

        synchronized (this) {

            noOfCompleted++;

            if (price != null) {
                if (resultPrice == null || resultPrice > price) {
                    resultPrice = price;
                }
            }

            if (noOfCompleted == 3) {
                sendResultAndTerminate();
            }
        }
    }

    private void handleDbUpdated(Integer noOfQuestions) {

        this.noOfQuestions = noOfQuestions;
        synchronized (this) {

            noOfCompleted++;

            if (noOfCompleted == 3) {
                sendResultAndTerminate();
            }
        }
    }

    private void sendResultAndTerminate() {
        ComparePricesResult result = null;
        if (resultPrice == null) {
            result = new ComparePricesResult("product not available; " + "product searched " + this.noOfQuestions + " times");
        } else {
            result = new ComparePricesResult("price: " + resultPrice.toString() + "; " + "product searched " + this.noOfQuestions + " times");
        }

        requestSender.tell(result, self());

        terminateWithChildren();
    }

    private void terminateWithChildren() {
        context().stop(shop1);
        context().stop(shop2);
        context().stop(dbHelper);
        context().stop(self());
    }
}
