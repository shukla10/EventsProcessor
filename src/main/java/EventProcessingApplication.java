import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EventProcessingApplication {
    static Logger logger = Logger.getLogger(EventProcessingApplication.class.getName());

    public static void main(String args[]) {
        EventProcessor eventProcessor = new EventProcessor();
        List<Event> eventList = new ArrayList<>();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));

        logger.info("Please Enter file location and press return:");
        try {
             eventList = eventProcessor.collectEventsFromFile(new File(reader.readLine()));
        } catch (IOException ex) {
            logger.error("IO Exception while reading file", ex);
        }

        eventProcessor.processEvents(eventList);

        List<EventDB> processedEvents = eventProcessor.readFromDb();
        logger.info("**Processed Events**");
        processedEvents.stream().forEach(eventDB -> {
            if(eventDB.type!=null && eventDB.host!=null) {
                logger.info(eventDB.id +" "+ eventDB.duration +" "+ eventDB.type +" "+ eventDB.host +" "+ eventDB.alert);
            } else {
                logger.info(eventDB.id +" "+ eventDB.duration +" "+ eventDB.alert);
            }
        });
    }
}
