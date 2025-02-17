/*
 * The MIT License
 * <p>
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.cs.admin.core.entity;

import ee.ria.xroad.common.util.NoCoverage;

import jakarta.persistence.Transient;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface EntityExistsAwareEntity<SELF extends EntityExistsAwareEntity<SELF>> {

    @SuppressWarnings("checkstyle:InterfaceIsType")
    interface ById<SELF extends ByIdInt<SELF>> extends EntityExistsAwareEntity<SELF> {
        String ID = "id";
    }

    interface ByIdOf<SELF extends ByIdInt<SELF>, ID> extends ById<SELF> {
        ID getId();
    }

    interface ByIdInt<SELF extends ByIdInt<SELF>> extends ById<SELF> {

        int getId();

        @Transient
        @NoCoverage
        default boolean exists() {
            return getId() > 0;
        }
    }

    @Transient
    boolean exists();

    @Transient
    default boolean notExists() {
        return !exists();
    }

    @Transient
    default Optional<SELF> ifExists(Consumer<SELF> consumer) {
        SELF self = (SELF) this;
        if (self.exists()) {
            consumer.accept(self);
            return Optional.of(self);
        }
        return Optional.empty();
    }


    @Transient
    default <R> Optional<R> ifExists(Function<SELF, R> function) {
        return Optional.of((SELF) this)
                .filter(SELF::exists)
                .map(function);
    }

    @Transient
    default SELF ifNotExists(Supplier<SELF> supplier) {
        SELF self = (SELF) this;
        return self.exists() ? self : supplier.get();
    }

    @Transient
    default <T extends RuntimeException> void ifExistsThrow(Supplier<T> throwsSupplier) {
        ifExists((Consumer<SELF>) __ -> {
            throw throwsSupplier.get();
        });
    }

    @Transient
    default <T extends RuntimeException> void ifExistsThrow(Function<SELF, T> throwsSupplier) {
        ifExists((Function<SELF, ?>) self -> {
            throw throwsSupplier.apply(self);
        });
    }

    @Transient
    default <T extends RuntimeException> SELF ifNotExistsThrow(Supplier<T> throwsSupplier) {
        return ifNotExists(() -> {
            throw throwsSupplier.get();
        });
    }
}
