package workshop.cinema.reservation.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import workshop.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import workshop.cinema.reservation.domain.Show;
import workshop.cinema.reservation.domain.ShowCommand;
import workshop.cinema.reservation.domain.ShowCommand.CreateShow;
import workshop.cinema.reservation.domain.ShowCommandError;
import workshop.cinema.reservation.domain.ShowCreator;
import workshop.cinema.reservation.domain.ShowEvent;
import workshop.cinema.reservation.domain.ShowEvent.ShowCreated;
import workshop.cinema.reservation.domain.ShowId;

import static workshop.cinema.reservation.domain.ShowCommandError.SHOW_NOT_EXISTS;

public class ShowEntity extends EventSourcedBehaviorWithEnforcedReplies<ShowEntityCommand, ShowEvent, Show> {

    public static final EntityTypeKey<ShowEntityCommand> SHOW_ENTITY_TYPE_KEY =
            EntityTypeKey.create(ShowEntityCommand.class, "Show");

    private final ShowId showId;
    private final Clock clock;
    private final ActorContext<ShowEntityCommand> context;

    private ShowEntity(PersistenceId persistenceId, ShowId showId, Clock clock, ActorContext<ShowEntityCommand> context) {
        super(persistenceId);
        this.showId = showId;
        this.clock = clock;
        this.context = context;
    }

    public static Behavior<ShowEntityCommand> create(ShowId showId,
                                                     Clock clock) {
        return Behaviors.setup(context -> {
            PersistenceId persistenceId = PersistenceId.of(SHOW_ENTITY_TYPE_KEY.name(), showId.id().toString());
            context.getLog().info("ShowEntity {} initialization started", showId);
            return new ShowEntity(persistenceId, showId, clock, context);
        });
    }

    @Override
    public Show emptyState() {
        return null;
    }

    @Override
    public CommandHandlerWithReply<ShowEntityCommand, ShowEvent, Show> commandHandler() {
        var builder = newCommandHandlerWithReplyBuilder();

        builder.forNullState()
                .onCommand(ShowEntityCommand.GetShow.class, this::returnEmptyState)
                .onCommand(ShowEntityCommand.ShowCommandEnvelope.class, this::handleShowCreation);

        builder.forStateType(Show.class)
                .onCommand(ShowEntityCommand.GetShow.class, this::returnState)
                .onCommand(ShowEntityCommand.ShowCommandEnvelope.class, this::handleShowCommand);

        return builder.build();
    }

    private ReplyEffect<ShowEvent, Show> handleShowCreation(ShowEntityCommand.ShowCommandEnvelope envelope) {
        ShowCommand command = envelope.command();
        if (command instanceof CreateShow createShow) {
            Either<ShowCommandError, List<ShowEvent>> processingResult = ShowCreator.create(createShow, clock).map(List::of);
            return handleResult(envelope, processingResult);
        } else {
            context.getLog().warn("Show {} not created", command.showId());
            return Effect().reply(envelope.replyTo(), new CommandRejected(SHOW_NOT_EXISTS));
        }
    }

    private ReplyEffect<ShowEvent, Show> handleShowCommand(Show show, ShowEntityCommand.ShowCommandEnvelope envelope) {
        Either<ShowCommandError, List<ShowEvent>> processingResult = show.process(envelope.command(), clock);
        return handleResult(envelope, processingResult);
    }

    private ReplyEffect<ShowEvent, Show> handleResult(ShowEntityCommand.ShowCommandEnvelope envelope, Either<ShowCommandError, List<ShowEvent>> processingResult) {
        ShowCommand command = envelope.command();
        return processingResult.fold(
                error -> {
                    context.getLog().info("Command rejected: {} with {}", command, error);
                    return Effect().reply(envelope.replyTo(), new CommandRejected(error));
                },
                events -> {
                    context.getLog().debug("Command handled: {}", command);
                    return Effect().persist(events.toJavaList())
                            .thenReply(envelope.replyTo(), s -> new CommandProcessed());
                }
        );
    }

    private ReplyEffect<ShowEvent, Show> returnEmptyState(ShowEntityCommand.GetShow getShow) {
        return Effect().reply(getShow.replyTo(), Option.none());
    }

    private ReplyEffect<ShowEvent, Show> returnState(Show show, ShowEntityCommand.GetShow getShow) {
        return Effect().reply(getShow.replyTo(), Option.of(show));
    }

    @Override
    public EventHandler<Show, ShowEvent> eventHandler() {
        EventHandlerBuilder<Show, ShowEvent> builder = newEventHandlerBuilder();

        builder.forNullState()
                .onEvent(ShowCreated.class, Show::create);

        builder.forStateType(Show.class)
                .onAnyEvent(Show::apply);

        return builder.build();
    }
}
