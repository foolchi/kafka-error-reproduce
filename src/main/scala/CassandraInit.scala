import com.datastax.driver.core.Cluster

/**
 * Created by foolchi on 29/07/15.
 * 建立一个简单的Cassandra数据库
 */
object CassandraInit {
  def main(args: Array[String]):Unit = {
    val cluster = Cluster.builder().addContactPoint("localhost").build()
    val cassandraDriver = cluster.newSession()
    cassandraDriver.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1}")
    cassandraDriver.execute("CREATE TABLE IF NOT EXISTS test.userlogs (user text, time bigint, log text, PRIMARY KEY (user, time))")
    val statement = cassandraDriver.prepare("INSERT INTO test.userlogs(user, time, log) VALUES (?, ?, ?)")

    // add some random data
    val r = scala.util.Random
    for (i <- 0 until 100) {
      val logval = r.nextInt(20)
      val log = if (logval < 15) logval.toString else logval.toString + " "
      cassandraDriver.execute(statement.bind(r.nextInt(10).toString, System.currentTimeMillis():java.lang.Long, log))
    }

    cassandraDriver.close()
    cluster.close()
  }
}
