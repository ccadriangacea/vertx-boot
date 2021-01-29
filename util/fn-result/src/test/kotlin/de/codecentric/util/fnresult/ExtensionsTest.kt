package de.codecentric.util.fnresult

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExtensionsTest {
    @Nested
    @Suppress("ConstantConditionIf")
    inner class PeekTests {
        @Test
        fun `should return the fnResult unchanged`() {
            handleThrowable { 1 }
                .peek { assertThat(it).isEqualTo(1) }
                .map {
                    assertThat(it).isEqualTo(1)
                    2
                }
                .peek { assertThat(it).isEqualTo(2) }
        }

        @Test
        fun `should return a fnError if peek throws`() {
            val result = handleThrowable { 1 }
                .peek {
                    assertThat(it).isEqualTo(1)
                    if (true) throw Exception("TEST")
                }
                .map {
                    assertThat(true).isFalse
                    2
                }

            assertThat(result).isInstanceOf(FnResult.FnError::class.java)
            assertThat((result as FnResult.FnError).errorMessage).isEqualTo("TEST")
        }
    }

    @Nested
    @Suppress("ConstantConditionIf")
    inner class FnResultFlatMapTests {
        @Test
        fun `should flatMap a fnResult when it is iterable`() {
            val result = FnResult.FnSuccess(listOf(1, 2, 3)).flatMap { listOf(it, it) }

            assertThat(result).isInstanceOf(FnResult.FnSuccess::class.java)
            assertThat(result.getResult()).containsExactly(1, 1, 2, 2, 3, 3)
            assertThat(result.getResult().size).isEqualTo(6)
        }

        @Test
        fun `should flatMap a fnResult when it is iterable and return just one list`() {
            val result = handleThrowable { listOf(1, 2, 3) }
                .flatMap { listOf(it, it) }
                .mapList { it * 2 }

            assertThat(result).isInstanceOf(FnResult.FnSuccess::class.java)
            assertThat(result.getResult()).containsExactly(2, 2, 4, 4, 6, 6)
            assertThat(result.getResult().size).isEqualTo(6)
        }

        @Test
        fun `should flatMap a fnResult to an fnError when mapFn throws error`() {
            val result = handleThrowable { listOf(1, 2, 3) }
                .flatMap {
                    if (true) throw Exception("TEST")
                    listOf(it, it)
                }

            assertThat(result).isInstanceOf(FnResult.FnError::class.java)
            assertThat((result as FnResult.FnError).errorMessage).isEqualTo("TEST")
        }

        @Test
        fun `should fail in first map and carry this exception not the one in flatMap`() {
            val result = handleThrowable { listOf(1, 2, 3) }
                .map {
                    if (true) throw Exception("TEST1")
                    it
                }
                .flatMap {
                    if (true) throw Exception("TEST2")
                    listOf(it, it)
                }

            assertThat(result).isInstanceOf(FnResult.FnError::class.java)
            assertThat((result as FnResult.FnError).errorMessage).isEqualTo("TEST1")
        }

        @Test
        fun `should fail in flatMap and carry this exception not the one in the next map`() {
            val result = handleThrowable { listOf(1, 2, 3) }
                .flatMap {
                    if (true) throw Exception("TEST1")
                    listOf(it, it)
                }
                .map {
                    if (true) throw Exception("TEST2")
                    it
                }

            assertThat(result).isInstanceOf(FnResult.FnError::class.java)
            assertThat((result as FnResult.FnError).errorMessage).isEqualTo("TEST1")
        }
    }
}