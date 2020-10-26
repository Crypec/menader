package menader.model;


import lombok.Data;
import menader.util.Util;
import org.apache.commons.lang3.RandomStringUtils;

@Data
public class Address {
    public String street;
    public int nr;
    public int zipCode;
    public String town;
    public String country;

    public static Address random() {
        var addr = new Address();
        addr.setStreet(RandomStringUtils.random(10, true, false));
        addr.setNr(Util.randomInt(1, 40));
        addr.setZipCode(Util.randomInt(8001, 8926));
        addr.setTown(RandomStringUtils.random(10, false, true));
        addr.setCountry("CH");
        return addr;
    }
}
