import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.codec.digest.DigestUtils
import java.nio.charset.StandardCharsets

class Client(private val connection: Connection) {
    fun solve(difficulty: Int) {
        val channel = connection.createChannel()

        channel.queueDeclare(TASK_QUEUE, false, false, true, null)
        channel.queueDeclare(SOLUTION_QUEUE, false, false, true, null)

        channel.basicConsume(
            SOLUTION_QUEUE,
            true,
            DeliverCallback { _, delivery ->
                val solution = String(delivery.body, StandardCharsets.UTF_8)

                println(
                    """
                        Solution: $solution
                        Hash: ${DigestUtils.sha256Hex(solution)}
                    """.trimIndent()
                )

                channel.close()
                connection.close()
            },
            CancelCallback {
                println("Cancel")
            }
        )

        channel.basicPublish(
            "",
            TASK_QUEUE,
            AMQP.BasicProperties.Builder().replyTo(SOLUTION_QUEUE).build(),
            difficulty.toString().toByteArray()
        )
        println("Solution requested")
    }
}