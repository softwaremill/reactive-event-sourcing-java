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
import static workshop.cinema.reservation.domain.DomainGenerators.randomSeatNumber;
import static workshop.cinema.reservation.domain.DomainGenerators.randomShowId;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class ShowControllerItTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void shouldGetShowById() {
        //given
        String showId = randomShowId().id().toString();

        //when //then
        webClient.get().uri("/shows/{showId}", showId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ShowResponse.class).value(shouldHaveId(showId));
    }

    @Test
    public void shouldReserveSeat() {
        //given
        String showId = randomShowId().id().toString();
        int seatNum = randomSeatNumber().value();

        //when //then
        webClient.patch().uri("/shows/{showId}/seats/{seatNum}", showId, seatNum)
                .bodyValue(new SeatActionRequest(RESERVE))
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    public void shouldNotReserveTheSameSeat() {
        //given
        String showId = randomShowId().id().toString();
        int seatNum = randomSeatNumber().value();

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
        String showId = randomShowId().id().toString();
        int seatNum = randomSeatNumber().value();

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

}