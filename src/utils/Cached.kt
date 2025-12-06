package utils

fun <K, V> cached(block1: Cache1<K, V>.(K) -> V) = Cache1(block1)
fun <K1, K2, V> cached2(block2: Cache2<K1, K2, V>.(K1, K2) -> V) = Cache2(block2)
fun <K1, K2, K3, V> cached3(block3: Cache3<K1, K2, K3, V>.(K1, K2, K3) -> V) = Cache3(block3)

abstract class Cache<K, V>(val items: MutableMap<K, V> = mutableMapOf()) {
    operator fun get(key: K): V = items.getOrPut(key) { calculate(key) }
    abstract fun calculate(key: K): V
}

class Cache1<K, V>(private val calculate1: Cache1<K, V>.(K) -> V) : Cache<K, V>() {
    operator fun invoke(key: K): V = get(key)
    override fun calculate(key: K): V = calculate1(key)
}

class Cache2<K1, K2, V>(private val calculate2: Cache2<K1, K2, V>.(K1, K2) -> V) : Cache<Pair<K1, K2>, V>() {
    operator fun invoke(key1: K1, key2: K2): V = get(key1 to key2)
    override fun calculate(key: Pair<K1, K2>): V = calculate2(key.first, key.second)
}

class Cache3<K1, K2, K3, V>(private val calculate3: Cache3<K1, K2, K3, V>.(K1, K2, K3) -> V) :
    Cache<Triple<K1, K2, K3>, V>() {
    operator fun invoke(key1: K1, key2: K2, key3: K3): V = get(Triple(key1, key2, key3))
    override fun calculate(key: Triple<K1, K2, K3>): V = calculate3(key.first, key.second, key.third)
}
