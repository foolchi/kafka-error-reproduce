/**
 * Created by foolchi on 29/07/15.
 * 从Cassandra中读取某一用户全部消息，
 * 以调试模式发送到kafka
 */

import java.util.Properties
import collection.JavaConverters._

import com.datastax.driver.core.{ResultSet, Cluster}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

object ReproduceFromCassandra {
  def main(args: Array[String]):Unit = {
    val host = "localhost"
    val user = "1"

    // prepare kafka producer
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("acks", "1")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    // connect to cassandra
    val cluster = Cluster.builder().addContactPoint(host).build()
    val cassandraDriver = cluster.connect()

    // select data and send to kafka
    val results:ResultSet = cassandraDriver.execute("SELECT * FROM test.userlogs WHERE user='" + user + "'")
    results.asScala.foreach{
      row =>
        val msg = "{\"user:\": \"%s\", \"log\": \"%s\", \"time\": %d, \"mode\": \"%s\"}".format(row.getString("user"), row.getString("log"), row.getLong("time"), "DEBUG")
        producer.send(new ProducerRecord[String, String]("test", row.getString("user"), msg))
    }

    cassandraDriver.close()
    cluster.close()

  }
}
