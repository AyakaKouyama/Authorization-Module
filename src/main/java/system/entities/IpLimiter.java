package system.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "\"ip_limiter\"")
@NamedQueries({
        @NamedQuery(name = "IpLimiter.getByIp",
                query = "SELECT u FROM IpLimiter u WHERE u.ip = :ip")
})

public class IpLimiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "ip", unique =  true, nullable = false)
    private String ip;

    @Column(name = "ip_counter", nullable = false)
    private Integer ipCounter;

    @Column(name = "blocked_to")
    private LocalDateTime blockedTo;

    public IpLimiter(){
    	//default public constructor
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getIpCounter() {
		return ipCounter;
	}

	public void setIpCounter(Integer ipCounter) {
		this.ipCounter = ipCounter;
	}

	public LocalDateTime getBlockedTo() {
		return blockedTo;
	}

	public void setBlockedTo(LocalDateTime blockedTo) {
		this.blockedTo = blockedTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IpLimiter)) return false;
		IpLimiter ipLimiter = (IpLimiter) o;
		return Objects.equals(getId(), ipLimiter.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
