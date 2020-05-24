package common;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import common.message.ComparePricesRequest;
import common.message.ComparePricesResult;
import common.message.GetPrice;
import common.message.UpdateDbRequest;
import scala.Int;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class PriceComparator extends AbstractActor {

    private Timeout timeout = Timeout.create(Duration.ofMillis(300));
    private Timeout dbTimeout = Timeout.create(Duration.ofMillis(13000));
    private Timeout awaitTimeout = Timeout.create(Duration.ofSeconds(5));
    private Integer resultPrice = null;
    private Integer noOfQuestions = 0;

    private Integer noOfCompleted = 0;

    private ActorRef shop1;
    private ActorRef shop2;
    private ActorRef dbHelper;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ComparePricesRequest.class, msg -> {

                    this.shop1 = context().actorOf(Props.create(Shop.class));
                    this.shop2 = context().actorOf(Props.create(Shop.class));
                    this.dbHelper = context().actorOf(Props.create(DbHelper.class));

                    Integer price1 = null;
                    Integer price2 = null;
                    Integer noOfSearches = null;

                    Future<Object> futurePrice1 = Patterns.ask(shop1, new GetPrice(), timeout);
                    Future<Object> futurePrice2 = Patterns.ask(shop2, new GetPrice(), timeout);
                    Future<Object> futureNoOfSearches = Patterns.ask(dbHelper, new UpdateDbRequest(msg.product), dbTimeout);

                    try {
                        price1 = (Integer) Await.result(futurePrice1, awaitTimeout.duration());
                        System.out.println("price1=" + price1);
                    } catch (TimeoutException ex) {
                        System.out.println("ex 1");
                    }

                    try {
                        price2 = (Integer) Await.result(futurePrice2, awaitTimeout.duration());
                        System.out.println("price2=" + price2);
                    } catch (TimeoutException ex) {
                        System.out.println("ex 2");
                    }

                    try {
                        noOfSearches = (Integer) Await.result(futureNoOfSearches, awaitTimeout.duration());
                        System.out.println("noof=" + noOfSearches);
                    } catch (TimeoutException ex) {
                        System.out.println("ex db");
                    }

                    ComparePricesResult result = calculateResult(price1, price2, noOfSearches);
                    sender().tell(result, self());

                    terminateWithChildren();

//                    this.requestSender = msg.sender;
//
//                    this.shop1 = context().actorOf(Props.create(Shop.class));
//                    this.shop2 = context().actorOf(Props.create(Shop.class));
//                    this.dbHelper = context().actorOf(Props.create(DbHelper.class));
//
//                    CompletableFuture<Object> futurePrice1 = ask(shop1, new GetPrice(), timeout).toCompletableFuture();
//                    CompletableFuture<Object> futurePrice2 = ask(shop2, new GetPrice(), timeout).toCompletableFuture();
//                    CompletableFuture<Object> futureNoOfQuestions = ask(dbHelper, new UpdateDbRequest(msg.product), dbTimeout).toCompletableFuture();
//
//                    futurePrice1.whenComplete((res, err) -> {
//                        handlePriceCompleted((Integer) res);
//                    });
//                    futurePrice2.whenComplete((res, err) -> {
//                        handlePriceCompleted((Integer) res);
//                    });
//                    futureNoOfQuestions.whenComplete((res, err) -> {
//                        handleDbUpdated((Integer) res);
//                    });

                })
                .build();
    }



    private ComparePricesResult calculateResult(Integer price1, Integer price2, Integer dbInfo) {
        if (price1 != null && price2 != null) {
            Integer price = (price1 < price2) ? price1 : price2;
            return new ComparePricesResult("price: " + price + "; " + "product searched " + dbInfo + " time(s)");
        }
        if (price1 != null) {
            return new ComparePricesResult("price: " + price1 + "; " + "product searched " + dbInfo + " time(s)");
        }
        if (price2 != null) {
            return new ComparePricesResult("price: " + price2 + "; " + "product searched " + dbInfo + " time(s)");
        }

        return new ComparePricesResult("product not available; " + "product searched " + dbInfo + " time(s)");
    }

    private void terminateWithChildren() {
        context().stop(shop1);
        context().stop(shop2);
        context().stop(dbHelper);
        context().stop(self());
    }
}
