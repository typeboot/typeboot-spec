package com.typeboot

import com.typeboot.dataformat.factory.TypeFactory
import java.io.File

fun main(args: Array<String>) {
    val filenames = if (args.size == 1) {
        args[0]
    } else {
        ".typeboot.yaml"
    }
    filenames.split(",").map { f -> f.trim() }.forEach { filename ->
        if (!File(filename).exists()) {
            throw RuntimeException("$filename not found")
        }
        val typeFactory = TypeFactory(filename)
        typeFactory.generate()
    }
}
