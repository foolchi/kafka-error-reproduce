package com.growingio.samza;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;

import java.util.Map;

/**
 * Created by foolchi on 29/07/15.
 * Test if the log is greater than 5
 */
public class SamzaStream5 implements StreamTask {

    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception{
        Map<String, Object> msg = (Map<String, Object>)envelope.getMessage();

        int logval;
        try {
            logval = Integer.parseInt(((String) msg.get("log")).trim());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (logval >= 5) {
            collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", "g5"), envelope.getKey(), msg));
        }

        // check if it's a debug message
        if (msg.containsKey("mode") && msg.get("mode").equals("DEBUG")){
            msg.put("path", "A");
            collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", "debug"), envelope.getKey(), msg));
        }
    }
}
