package me.tatarka.domain.mapper.test

data class ModelOneApi(val one: String, val two: Int)
data class ModelOneDomain(val one: String, val two: Int)

data class ModelTwoApi(val same: String, val different: String, val extra: String)
data class ModelTwoDomain(val same: String, val different: Int, val new: String)