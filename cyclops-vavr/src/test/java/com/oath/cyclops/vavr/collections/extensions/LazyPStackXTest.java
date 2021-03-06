package com.oath.cyclops.vavr.collections.extensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.collections.immutable.BagX;
import cyclops.reactive.collections.immutable.LinkedListX;
import org.junit.Test;


import cyclops.collections.vavr.VavrListX;


import reactor.core.publisher.Flux;

public class LazyPStackXTest extends AbstractOrderDependentCollectionXTest  {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        LinkedListX<T> list = VavrListX.empty();
        for (T next : values) {
            list = list.insertAt(list.size(), next);
        }
        System.out.println("List " + list);
        return list;

    }

    @Test
    public void onEmptySwitch() {
        assertThat(VavrListX.empty()
                          .onEmptySwitch(() -> LinkedListX.of(1, 2, 3)),
                   equalTo(LinkedListX.of(1, 2, 3)));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.functions.collections.extensions.AbstractCollectionXTest#
     * empty()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return VavrListX.empty();
    }



    @Test
    public void remove() {

        VavrListX.of(1, 2, 3)
               .removeAll((Iterable<Integer>) BagX.of(2, 3))
               .mergeMap(i -> Flux.just(10 + i, 20 + i, 30 + i));

    }

    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return VavrListX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return VavrListX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return VavrListX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return VavrListX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return VavrListX.unfold(seed, unfolder);
    }
}
