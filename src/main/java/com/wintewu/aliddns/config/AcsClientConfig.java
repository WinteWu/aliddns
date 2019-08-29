package com.wintewu.aliddns.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.wintewu.aliddns.config.properties.DDNSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Client客户端Bean
 * @author WinteWu
 */
@Configuration
public class AcsClientConfig {

    @Autowired
    private DDNSProperties ddnsProperties;

    @Bean
    public IAcsClient acsClientBean(){
        String regionId = ddnsProperties.getRegionId();
        String accessKeyId = ddnsProperties.getAccessKeyId();
        String accessKeySecret = ddnsProperties.getAccessKeySecret();
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        // 若报Can not find endpoint to access异常，请添加以下此行代码
        // DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Alidns", "alidns.aliyuncs.com");
        return new DefaultAcsClient(profile);
    }
}
