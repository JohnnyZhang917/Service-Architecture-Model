/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.sam.idgenerator;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class IdFactorialGenerator {

    public static int numberOfCommonPrimes = 20;

    private static AtomicInteger nextPrimeNumber = new AtomicInteger();
    private static final int[] commonPrimes = new int[numberOfCommonPrimes];

    static {
        for (int i = 0; i < numberOfCommonPrimes; i++) {
            commonPrimes[i] = Sieve$.MODULE$.primeNumber(nextPrimeNumber.getAndAdd(1));
        }
    }

    private int usedSlots;
    private long[] primes = new long[64];
    private long[] factors = new long[64];
    private long id;

    public synchronized static IdFactorialGenerator createGenerator() {
        return new IdFactorialGenerator();
    }

    private IdFactorialGenerator() {
        usedSlots = numberOfCommonPrimes + 1;
        for (int i = 0; i < numberOfCommonPrimes; i++) {
            primes[i] = commonPrimes[i];
            factors[i] = 1;
        }
        primes[numberOfCommonPrimes] = Sieve$.MODULE$.primeNumber(nextPrimeNumber.getAndAdd(1));
        factors[numberOfCommonPrimes] = primes[numberOfCommonPrimes];
        id = primes[numberOfCommonPrimes];
    }

    public long[] getConfig() {
        return Arrays.copyOf(primes, 64);
    }

    public long nextId() {
        for (int factorToUpdate = 0; factorToUpdate < 64; factorToUpdate++) {
            if (factorToUpdate == usedSlots) {
                factors[factorToUpdate] = 1;
                primes[factorToUpdate] = Sieve$.MODULE$.primeNumber(nextPrimeNumber.getAndAdd(1));
                usedSlots++;
            }
            long primeToExtend = primes[factorToUpdate];
            if (primeToExtend < Long.MAX_VALUE / id) {
                // id * primeToExtend < Long.MAX_VALUE
                factors[factorToUpdate] = factors[factorToUpdate] * primeToExtend;
                id = id * primeToExtend;
                return id;
            } else {
                factors[factorToUpdate] = 1;
                id = 1;
                for (int i = 0; i < usedSlots; i++) {
                    id = id * factors[i];
                }
            }
        }
        throw new IllegalStateException("I can not generate more ids");
    }
}
