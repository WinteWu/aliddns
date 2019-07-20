package com.wintewu.aliddns.config.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author wintewu
 */
@Component
@ConfigurationProperties(prefix = DDNSProperties.PREFIX)
public class DDNSProperties {
    public static final String PREFIX = "ddns";

    private String regionId = "cn-hangzhou";

    private String accessKeyId;

    private String accessKeySecret;

    private String domainName;

    private String RR;

    private Long ttl = 600L;

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getRR() {
        return RR;
    }

    public void setRR(String RR) {
        this.RR = RR;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public List<String> getRrs() {
        if (StringUtils.isNotBlank(this.RR)) {
            String[] rrs = StringUtils.split(this.RR, ",");
            return Arrays.asList(rrs);
        }
        return Arrays.asList(this.RR);
    }
}
