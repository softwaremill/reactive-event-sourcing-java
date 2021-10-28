package workshop.cinema.reservation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import workshop.cinema.reservation.application.ShowEntityResponse;
import workshop.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import workshop.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import workshop.cinema.reservation.application.ShowService;
import workshop.cinema.reservation.domain.SeatNumber;
import workshop.cinema.reservation.domain.ShowId;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> create(@RequestBody CreateShowRequest request) {
        CompletionStage<ResponseEntity<String>> showResponse = showService.createShow(ShowId.of(request.showId()), request.title(), request.maxSeats())
                .thenApply(result -> switch (result) {
                    case CommandProcessed ignored -> new ResponseEntity<>("Show created", CREATED);
                    case CommandRejected rejected -> transformRejection(rejected);
                });

        return Mono.fromCompletionStage(showResponse);
    }

    private ResponseEntity<String> transformRejection(CommandRejected rejected) {
        return switch (rejected.error()) {
            case SHOW_ALREADY_EXISTS -> new ResponseEntity<>("Show already created", CONFLICT);
            default -> badRequest().body("Show creation failed with: " + rejected.error().name());
        };
    }

    @GetMapping(value = "{showId}", produces = "application/json")
    public Mono<ResponseEntity<ShowResponse>> findById(@PathVariable UUID showId) {
        CompletionStage<ResponseEntity<ShowResponse>> showResponse = showService.findShowBy(ShowId.of(showId)).thenApply(result ->
                result.map(ShowResponse::from).map(ok()::body)
                        .getOrElse(notFound().build()));
        return Mono.fromCompletionStage(showResponse);
    }

    @PatchMapping(value = "{showId}/seats/{seatNum}", consumes = "application/json")
    public Mono<ResponseEntity<String>> reserve(@PathVariable("showId") UUID showIdValue,
                                                @PathVariable("seatNum") int seatNumValue,
                                                @RequestBody SeatActionRequest request) {

        ShowId showId = ShowId.of(showIdValue);
        SeatNumber seatNumber = SeatNumber.of(seatNumValue);
        CompletionStage<ShowEntityResponse> actionResult = switch (request.action()) {
            case RESERVE -> showService.reserveSeat(showId, seatNumber);
            case CANCEL_RESERVATION -> showService.cancelReservation(showId, seatNumber);
        };

        return Mono.fromCompletionStage(actionResult.thenApply(response -> switch (response) {
            case CommandProcessed ignored -> accepted().body(request.action() + " successful");
            case CommandRejected rejected -> badRequest().body(request.action() + " failed with: " + rejected.error().name());
        }));
    }
}
