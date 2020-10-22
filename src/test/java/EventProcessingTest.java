import Util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class EventProcessingTest {
    @Test
    public void test_givenLogFileContainsAllValidEvents_allEventsProcessedCorrectly() {
        File testFile = FileUtils.getFile("valid_logfile.txt");

        EventProcessor eventProcessor = new EventProcessor();
        List<Event> eventList = eventProcessor.collectEventsFromFile(testFile);
        List<String> eventsIdList = eventList.stream().map(event -> event.id).collect(Collectors.toList());
        // Log file have 2 entries for each event based on state, while there is one entry per event is stored after events processing
        // so filter distinct
        List<String> distinctEventsId = eventsIdList.stream().distinct().collect(Collectors.toList());

        // Process events
        eventProcessor.processEvents(eventList);

        // Fetch id of processed events
        List<EventDB> processedEventList = eventProcessor.readFromDb();
        List<String>  processedEventsIdList = processedEventList.stream().map(eventDB -> eventDB.id).collect(Collectors.toList());

        // Validate processed events
        for(String eventId : distinctEventsId) {
            assertTrue("Event id " + eventId + " was not processed !", processedEventsIdList.contains(eventId));
        }
    }

    @Test
    public void test_givenLogFileContainsSomeInvalidEvents_onlyValidEventsProcessedCorrectly() {
        /* Log file containing 3 event entry
         * 1. event with start
         * 2. event with finished state
         * 3. event with only start state - this will not be processed
         */

        File testFile = FileUtils.getFile("invalid_logfile.txt");

        EventProcessor eventProcessor = new EventProcessor();
        List<Event> eventList = eventProcessor.collectEventsFromFile(testFile);
        // Log file have 2 entries for each event based on state, while there is one entry per event is stored after events processing
        // so filter distinct & valid events
        Map<String, List<Event>> eventsById = eventList.stream().collect(Collectors.groupingBy(event -> event.id));
        List<String> validEvents = new ArrayList<>();
        eventsById.forEach((eventId, eventsGroup)-> {
            List<String> eventsStateInGroup = eventsGroup.stream().map(event -> event.state).collect(Collectors.toList());
            // validate there are 2 events per eventId and with both STARTED & FINISHED state
            if(eventsGroup.size() == 2 && eventsStateInGroup.contains("STARTED") && eventsStateInGroup.contains("FINISHED")) {
                validEvents.add(eventId);
            }
        });

        // Process events
        eventProcessor.processEvents(eventList);
        // Fetch id of processed events
        List<EventDB> processedEventList = eventProcessor.readFromDb();
        List<String>  processedEventsIdList = processedEventList.stream().map(eventDB -> eventDB.id).collect(Collectors.toList());


        for(String eventId : validEvents) {
            assertTrue("Event id " + eventId + " was not processed !", processedEventsIdList.contains(eventId));
        }
    }
}
