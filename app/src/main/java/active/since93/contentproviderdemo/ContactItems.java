package active.since93.contentproviderdemo;

/**
 * Created by darshan.parikh on 24-Sep-15.
 */
public class ContactItems {
    String name;
    String number;

    public ContactItems(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
