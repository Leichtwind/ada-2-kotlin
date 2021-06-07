import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.codec.digest.DigestUtils
import java.nio.charset.StandardCharsets

class Worker(private val connection: Connection) {
    fun start() {
        val channel = connection.createChannel()

        channel.queueDeclare(TASK_QUEUE, false, false, true, null)

        channel.basicConsume(
            TASK_QUEUE,
            true,
            DeliverCallback { _, delivery ->
                val difficulty = String(delivery.body, StandardCharsets.UTF_8).toInt()
                println("Solving crypto puzzle with difficulty of $difficulty");

                val solution = solveCryptoPuzzle(difficulty) ?: "NOT FOUND"
                println(
                    """
                        Solution: $solution
                        Hash: ${DigestUtils.sha256Hex(solution)}
                    """.trimIndent()
                )

                channel.basicPublish("", delivery.properties.replyTo, null, solution.toByteArray())
            },
            CancelCallback {
                println("Cancel")
            }
        )

        println("Listening")
    }

    private fun solveCryptoPuzzle(difficulty: Int): String? {
        val nonce = "0".repeat(difficulty)

        for (i in 0..Long.MAX_VALUE) {
            val solutionCandidate = BASE_STRING + i

            if (DigestUtils.sha256Hex(solutionCandidate).startsWith(nonce)) {
                return solutionCandidate
            }
        }

        return null
    }
}
