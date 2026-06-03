package com.Abhi.job_finder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "job")
public class JobDomainsConfig {
    private List<JobDomain> domains;
    public List<JobDomain> getDomains() { return domains; }
    public void setDomains(List<JobDomain> domains) { this.domains = domains; }
}