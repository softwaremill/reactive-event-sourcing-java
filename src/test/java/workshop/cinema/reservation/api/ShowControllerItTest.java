package workshop.cinema.reservation.api;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import static workshop.cinema.reservation.api.Action.CANCEL_RESERVATION;
import static workshop.cinema.reservation.api.Action.RESERVE;
import static workshop.cinema.reservation.domain.DomainGenerators.randomShowId;

/**
 * Run `docker-compose -f docker-compose-jdbc.yml up` in `development` folder
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class ShowControllerItTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void shouldCreateShow() {
        //given
        var createShowRequest = new CreateShowRequest(randomShowId().id(), "title", 10);

        //when //then
        createShow(createShowRequest);
    }

    @Test
    public void shouldGetShowById() {
        //given
        var createShowRequest = new CreateShowRequest(randomShowId().id(), "title", 10);
        var showId = createShowRequest.showId().toString();
        createShow(createShowRequest);

        //when //then
        webClient.get().uri("/shows/{showId}", showId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ShowResponse.class).value(shouldHaveId(showId));
    }

    @Test
    public void shouldGetNotFoundForNotExistingShow() {
        //given
        var showId = randomShowId().id().toString();

        //when //then
        webClient.get().uri("/shows/{showId}", showId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void shouldReserveSeat() {
        //given
        var createShowRequest = new CreateShowRequest(randomShowId().id(), "title", 10);
        var showId = createShowRequest.showId().toString();
        createShow(createShowRequest);
        int seatNum = 8;

        //when //then
        webClient.patch().uri("/shows/{showId}/seats/{seatNum}", showId, seatNum)
                .bodyValue(new SeatActionRequest(RESERVE))
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    public void shouldNotReserveTheSameSeat() {
        //given
        var createShowRequest = new CreateShowRequest(randomShowId().id(), "title", 10);
        var showId = createShowRequest.showId().toString();
        createShow(createShowRequest);
        int seatNum = 8;

        //when //then
        webClient.patch().uri("/shows/{showId}/seats/{seatNum}", showId, seatNum)
                .bodyValue(new SeatActionRequest(RESERVE))
                .exchange()
                .expectStatus().isAccepted();

        //when //then
        webClient.patch().uri("/shows/{showId}/seats/{seatNum}", showId, seatNum)
                .bodyValue(new SeatActionRequest(RESERVE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void shouldCancelReservation() {
        //given
        var createShowRequest = new CreateShowRequest(randomShowId().id(), "title", 10);
        var showId = createShowRequest.showId().toString();
        createShow(createShowRequest);
        int seatNum = 8;

        //when //then
        webClient.patch().uri("/shows/{showId}/seats/{seatNum}", showId, seatNum)
                .bodyValue(new SeatActionRequest(RESERVE))
                .exchange()
                .expectStatus().isAccepted();

        //when //then
        webClient.patch().uri("/shows/{showId}/seats/{seatNum}", showId, seatNum)
                .bodyValue(new SeatActionRequest(CANCEL_RESERVATION))
                .exchange()
                .expectStatus().isAccepted();
    }


    private BaseMatcher<ShowResponse> shouldHaveId(String showId) {
        return new BaseMatcher<>() {
            @Override
            public void describeTo(Description description) {
                description.appendValue("ShowResponse should with id: " + showId);
            }

            @Override
            public boolean matches(Object o) {
                if (o instanceof ShowResponse showResponse) {
                    return showResponse.id().equals(showId);
                } else {
                    return false;
                }
            }
        };
    }

    private void createShow(CreateShowRequest createShowRequest) {
        webClient.post().uri("/shows")
                .bodyValue(createShowRequest)
                .exchange()
                .expectStatus().isCreated();
    }

}