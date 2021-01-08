package com.typeboot

import com.typeboot.dataformat.factory.TypeFactory

fun main() {
    val cassandraGenerator = TypeFactory(".typeboot.yaml")
    cassandraGenerator.generate()

    val postgresGenerator = TypeFactory(".postgres.yaml")
    postgresGenerator.generate()
}
