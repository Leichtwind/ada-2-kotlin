import com.rabbitmq.client.ConnectionFactory

fun main(args: Array<String>) {
    val connection = ConnectionFactory()
        .apply { setUri(System.getenv("RABBITMQ_URL")) }
        .newConnection()

    when (args[0]) {
        "worker" -> Worker(connection).start()
        "client" -> {
            val difficulty = args[1].toInt()

            Client(connection).solve(difficulty)
        }
        else -> {
            throw Exception("Unknown command")
        }
    }
}