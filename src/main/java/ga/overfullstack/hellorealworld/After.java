package ga.overfullstack.hellorealworld;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.immutables.value.Value;

public class After {

  /**
   * ! The below validate lambdas has boilerplate code. But this is only for demo. To see how to
   * effectively write such validations, please refer to this talk:
   * https://www.youtube.com/watch?v=Dvr6gx4XaD8&list=PLrJbJ9wDl9EC0bG6y9fyDylcfmB_lT_Or&index=1
   */
  static final Function<ImmutableEgg, Either<Tuple2<ID, Failure>, ImmutableEgg>> validateAge =
      eggToValidate ->
          Either.<Tuple2<ID, Failure>, ImmutableEgg>right(eggToValidate)
              .filterOrElse(
                  egg -> egg.age() < 10, rottenEgg -> Tuple.of(rottenEgg.id(), Failure.ROTTEN));

  static final Function<ImmutableEgg, Either<Tuple2<ID, Failure>, ImmutableEgg>> validateField1 =
      eggToValidate ->
          Either.<Tuple2<ID, Failure>, ImmutableEgg>right(eggToValidate)
              .filterOrElse(
                  egg -> !egg.field1().isEmpty(),
                  rottenEgg -> Tuple.of(rottenEgg.id(), Failure.FIELD_1_ERROR));

  // Dummy function
  static List<Either<Tuple2<ID, Failure>, ImmutableEgg>> filterDuplicates(
      List<ImmutableEgg> eggsFromRequest) {
    return List.of();
  }

  static Either<Tuple2<ID, Failure>, ImmutableEgg> validate(
      Either<Tuple2<ID, Failure>, ImmutableEgg> eggToValidate) {
    return eggToValidate.flatMap(validateAge).flatMap(validateField1);
  }

  // Dummy Function
  static Either<Tuple2<ID, Failure>, EggEntity> mapToEntityObj(
      Either<Tuple2<ID, Failure>, ImmutableEgg> egg) {
    return Either.right(new EggEntity());
  }

  // Dummy function
  static Either<DMLOperationException, List<EggEntity>> bulkInsertIntoDB(
      List<EggEntity> eggEntityObjs) {
    return Either.right(List.of(new EggEntity()));
  }

  void orchestrate() {
    var immutableEggsFromRequest = List.<ImmutableEgg>of();
    final var partition =
        filterDuplicates(immutableEggsFromRequest).stream()
            .map(After::validate)
            .map(After::mapToEntityObj)
            .collect(Collectors.partitioningBy(Either::isRight));
    final var objsToInsert = partition.get(true).stream().map(Either::get).toList();
    bulkInsertIntoDB(objsToInsert);
  }

  enum Failure {
    ROTTEN,
    FIELD_1_ERROR
  }

  @Value.Immutable
  interface ID {
    int value();
  }

  @Value.Immutable
  interface Egg {

    ID id();

    int age();

    String field1();

    String field2();

    String field3();
  }

  private static class DMLOperationException extends Exception {}
}
