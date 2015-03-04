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

package eu.pmsoft.sam.idgenerator

import org.testng.annotations.Test
import scala.collection.parallel.immutable.{ParSeq, ParRange}

class IdGenerationTest {

  @Test
  def primeList() {
    val expected = Seq(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
    val generatedPrimes = (0 to 10).map(Sieve.primeNumber _)
    assert((expected zip generatedPrimes).filter(pair => pair._1 != pair._2).isEmpty)
  }

  @Test
  def testGeneratorsHaveDifferentPrimeNumbers() {
    val probsNr = 10000
    val primeSets = ParRange(0, probsNr, 1, true).map(_ => getThreadConfig()).toMap.map(_._2).map(_.toSeq.filter(_ > 0)).toSeq
    val toCompare = for {
      one <- (0 to primeSets.size - 1)
      two <- (0 to primeSets.size - 1)
      if (one < two)
    } yield (primeSets(one), primeSets(two))
    val repeatedids = toCompare.map(pair => pair._1.intersect(pair._2).drop(IdFactorialGenerator.numberOfCommonPrimes)).filterNot(_.isEmpty)
    println(repeatedids)
    assert(repeatedids.isEmpty)
  }

  @Test
  def testSinglePrimeFactorGenerator() {
    val probsNr = 100
    val idSets: ParSeq[Seq[Long]] = ParRange(0, probsNr, 1, true).map(_ => generateIds()).toSeq
    val pairsToCompare = for {
      one <- (0 to probsNr)
      two <- (0 to probsNr)
      if (one < two)
    } yield (idSets(one), idSets(two))
    val repeatedids = pairsToCompare.map(pair => pair._1.intersect(pair._2)).filterNot(_.isEmpty)
    assert(repeatedids.isEmpty)
  }

  val threadGenerator: ThreadLocal[IdFactorialGenerator] = new ThreadLocal[IdFactorialGenerator]() {
    override def initialValue(): IdFactorialGenerator = IdFactorialGenerator.createGenerator()
  }

  private def getThreadConfig() = {
    val generatedIds = (0 to 100).map(_ => threadGenerator.get().nextId())
    val generator = threadGenerator.get()
    generator -> generator.getConfig
  }

  private def generateIds() = {
    val generatedIds = (0 to 100).map(_ => threadGenerator.get().nextId())
    generatedIds.toSeq
  }


}
