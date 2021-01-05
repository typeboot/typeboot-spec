package com.typeboot.dataformat.types

data class Instructions(private val content: String) {
    fun text(): String = this.content
    override fun toString(): String = text()
}

data class Serialisation(val subPath: String, val extension: String)