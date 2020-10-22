import Util.ConnectionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventProcessor {
    static Logger logger = Logger.getLogger(EventProcessor.class.getName());
    private static final String STARTED = "STARTED";
    private static final String FINISHED = "FINISHED";


    public List<Event> collectEventsFromFile(File eventsLogFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Event> eventCollection = new ArrayList<>();
        String line = null;

        // Read Each line (event) and prepare list of events
        try(LineIterator it = FileUtils.lineIterator(eventsLogFile, StandardCharsets.UTF_8.toString())) {
            while (it.hasNext()) {
                line = it.nextLine();
                eventCollection.add(objectMapper.readValue(line, Event.class));
            }
        } catch (JsonProcessingException ex) {
            logger.error("Exception during serialization of line : "+line, ex);
        } catch (IOException ex) {
            logger.error("IO Exception while extracting events from file", ex);
        }

        return eventCollection;
    }

    public void processEvents(List<Event> eventList) {
        /* Process events from list i.e.
         * 1. Create a map which contains events grouped by event id. i.e. for each key in map, there will be 2 entries
         *      i) with state STARTED
         *      ii) with state FINISHED
         * 2. For each entry in map, determine duration and write to DB with flag accordingly
         */

        Map<String, List<Event>> eventsById = eventList.stream().collect(Collectors.groupingBy(event -> event.id));

        eventsById.forEach((eventId, eventsGroup)-> {
            EventDB eventDBEntry;
            List<String> eventsStateInGroup = eventsGroup.stream().map(event -> event.state).collect(Collectors.toList());

            // validate there are 2 events per eventId and with both STARTED & FINISHED state
            if(eventsGroup.size() == 2 && eventsStateInGroup.contains(STARTED) && eventsStateInGroup.contains(FINISHED)) {
                long eventDuration = Math.abs(eventsGroup.get(0).timestamp - eventsGroup.get(1).timestamp);
                String eventType = eventsGroup.get(0).type;
                String host = eventsGroup.get(0).host;

                if(Math.abs(eventsGroup.get(0).timestamp - eventsGroup.get(1).timestamp) > 4) {
                    eventDBEntry = new EventDB(eventId, eventDuration, eventType, host, true);

                } else {
                    eventDBEntry = new EventDB(eventId, eventDuration, eventType, host, false);
                }
                writeToDb(eventDBEntry);
            }
        });
    }

    public static void writeToDb(EventDB eventDB) {
        Transaction transaction = null;
        try (Session session = ConnectionUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(eventDB);
            transaction.commit();
            logger.info("Processed event with id : "+ eventDB.id);
        } catch (Exception ex) {
            logger.error("Exception while writing event " + eventDB.id + "to DB", ex);
        }
    }

    public static List<EventDB> readFromDb() {
        List<EventDB> eventsList = new ArrayList<>();
        try (Session session = ConnectionUtil.getSessionFactory().openSession()) {
            eventsList = session.createQuery("from EventDB", EventDB.class).list();
            return eventsList;
        } catch (Exception ex) {
            logger.error("Exception while reading processed event from DB", ex);
        }
        return eventsList;
    }
}
