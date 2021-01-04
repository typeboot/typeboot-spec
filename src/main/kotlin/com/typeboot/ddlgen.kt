package com.typeboot

import com.typeboot.dataformat.factory.TypeFactory

fun main() {
    val typeFactory = TypeFactory(".typeboot.yaml")
    typeFactory.generate()
}
