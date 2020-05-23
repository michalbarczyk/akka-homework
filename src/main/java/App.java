import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import message.UpdateDbRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class App {

    private static final String systemName = "comparison_sys";
    private static final int noOfClients = 10;

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        List<ActorRef> clients = new ArrayList<>();

        final ActorSystem system = ActorSystem.create(systemName);
        final ActorRef serverRef = system.actorOf(Props.create(Server.class), "server");

        IntStream.range(0, noOfClients).forEach(i -> {
            clients.add(system.actorOf(Props.create(Client.class)));
        });

//        IntStream.range(0, noOfClients).forEach(i -> {
//            clients.get(i).tell("msg"+i, null);
//        });




        while (true) {
            System.out.println("enter [client id] [item name]");
            String line = br.readLine();
            String[] tokens = line.split( " ");
            if (tokens.length == 2) {
                int id = Integer.parseInt(tokens[0]);
                String msg = tokens[1];
                clients.get(id).tell(msg, null);
            }
        }



    }
}
