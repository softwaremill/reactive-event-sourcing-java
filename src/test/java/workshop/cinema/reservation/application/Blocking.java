package workshop.cinema.reservation.application;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Blocking {

    public static <T> T await(CompletionStage<T> completionStage) throws ExecutionException, InterruptedException {
        return completionStage.toCompletableFuture().get();
    }
}
