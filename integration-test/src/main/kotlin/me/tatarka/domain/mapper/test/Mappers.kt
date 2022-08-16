package me.tatarka.domain.mapper.test

import me.tatarka.domain.mapper.annotations.Mapper

fun ModelOneApi.toDomain(): ModelOneDomain = with(ModelOneMapper()) { toDomain() }
fun ModelOneDomain.toApi(): ModelOneApi = with(ModelOneMapper()) { toApi() }

@Mapper
internal interface ModelOneMapper {
    fun ModelOneApi.toDomain(): ModelOneDomain
    fun ModelOneDomain.toApi(): ModelOneApi
}

fun ModelTwoApi.toDomain(): ModelTwoDomain = with(ModelTwoMapper()) { toDomain(different = different.toInt()) }
fun ModelTwoDomain.toApi(): ModelTwoApi = with(ModelTwoMapper()) { toApi(different = different.toString()) }

@Mapper
internal interface ModelTwoMapper {
    fun ModelTwoApi.toDomain(different: Int, new: String = "new"): ModelTwoDomain
    fun ModelTwoDomain.toApi(different: String, extra: String = "extra"): ModelTwoApi
}