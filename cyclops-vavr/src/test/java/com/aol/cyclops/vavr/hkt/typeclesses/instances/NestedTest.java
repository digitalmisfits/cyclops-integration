package com.aol.cyclops.vavr.hkt.typeclesses.instances;

import com.aol.cyclops.vavr.hkt.ListKind;
import com.aol.cyclops.vavr.hkt.StreamKind;
import com.aol.cyclops2.hkt.Higher;
import com.google.common.collect.FluentIterable;
import cyclops.companion.Monoids;
import cyclops.companion.vavr.Lists;
import cyclops.companion.vavr.Streams;
import cyclops.companion.vavr.Streams.StreamNested;
import cyclops.monads.VavrWitness;
import cyclops.monads.VavrWitness.list;

import cyclops.monads.VavrWitness.stream;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class NestedTest {

    @Test
    public void listOption(){
        Higher<list, Integer> res = Lists.ListNested
                                         .option(List.of(Option.some(1)))
                                         .map(i -> i * 20)
                                         .foldsUnsafe()
                                         .foldLeft(Monoids.intMax);
        List<Integer> fi = ListKind.narrow(res);
        assertThat(fi.get(0),equalTo(20));
    }
    @Test
    public void streamEither(){
        Higher<stream, Integer> res = StreamNested.either(Stream.of(Either.right(1)))
                                                  .map(i -> i * 20)
                                                  .foldsUnsafe()
                                                  .foldLeft(Monoids.intMax);
        Stream<Integer> fi = StreamKind.narrow(res);
        assertThat(fi.get(0),equalTo(20));
    }

}

