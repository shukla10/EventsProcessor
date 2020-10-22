import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EventDB {
    @Id
    String id;
    long duration;
    String type;
    String host;
    boolean alert;

    public EventDB(String id, long duration, String type, String host, boolean alert) {
        this.id = id;
        this.duration = duration;
        this.type = type;
        this.host = host;
        this.alert = alert;
    }

    public EventDB() {
    }
}
