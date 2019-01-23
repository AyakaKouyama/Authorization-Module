package system.web.dtos;

import com.andrew.modelmapper.annotations.MmConvertedValue;
import com.andrew.modelmapper.annotations.MmValue;
import com.andrew.modelmapper.core.LocalDateTimeConverter;

import javax.json.bind.annotation.JsonbNillable;
import javax.json.bind.annotation.JsonbPropertyOrder;


@JsonbNillable
@JsonbPropertyOrder({"id", "ip", "blocked_to"})
public class IpLimiterDto {

    @MmValue(fieldName = "id")
    private Long id;

    @MmValue(fieldName = "ip")
    private String ip;

    @MmValue(fieldName = "ipCounter")
    private Integer ipCounter;

    @MmConvertedValue(fieldName = "blockedTo", convertertClass = LocalDateTimeConverter.class)
    private String blockedTo;

    public IpLimiterDto() {
        //default public constructor
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

   public Integer getIpCounter() { return ipCounter; }

   public void setIpCounter(Integer ipCounter) { this.ipCounter = ipCounter;}

   public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBlockedTo() { return blockedTo; }

    public void setBlockedTo(String blockedTo) {this.blockedTo = blockedTo;}
}

