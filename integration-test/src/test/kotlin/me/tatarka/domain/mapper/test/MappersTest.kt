package me.tatarka.domain.mapper.test

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.junit.jupiter.api.Test

class MappersTest {
    @Test
    fun `converts ModelOne api to domain`() {
        val api = ModelOneApi(one = "one", two = 2)
        val domain = api.toDomain()

        assertThat(domain).all {
            prop(ModelOneDomain::one).isEqualTo("one")
            prop(ModelOneDomain::two).isEqualTo(2)
        }
    }

    @Test
    fun `converts ModelOne domain to api`() {
        val domain = ModelOneDomain(one = "one", two = 2)
        val api = domain.toApi()

        assertThat(api).all {
            prop(ModelOneApi::one).isEqualTo("one")
            prop(ModelOneApi::two).isEqualTo(2)
        }
    }

    @Test
    fun `converts ModelTwo api to domain`() {
        val api = ModelTwoApi(same = "same", different = "2", extra = "extra")
        val domain = api.toDomain()

        assertThat(domain).all {
            prop(ModelTwoDomain::same).isEqualTo("same")
            prop(ModelTwoDomain::different).isEqualTo(2)
            prop(ModelTwoDomain::new).isEqualTo("new")
        }
    }

    @Test
    fun `converts ModelTwo domain to api`() {
        val domain = ModelTwoDomain(same = "same", different = 2, new = "new")
        val api = domain.toApi()

        assertThat(api).all {
            prop(ModelTwoApi::same).isEqualTo("same")
            prop(ModelTwoApi::different).isEqualTo("2")
            prop(ModelTwoApi::extra).isEqualTo("extra")
        }
    }
}