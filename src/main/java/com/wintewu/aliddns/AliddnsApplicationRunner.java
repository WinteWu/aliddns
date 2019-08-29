package com.wintewu.aliddns;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.wintewu.aliddns.common.cache.CacheManager;
import com.wintewu.aliddns.config.properties.DDNSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 应用启动时 校验一遍配置信息
 * @author WinteWu
 */
@Component
@Order(1)
public class AliddnsApplicationRunner implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(AliddnsApplicationRunner.class);

    @Autowired
    private DDNSProperties ddnsProperties;

    @Autowired
    private IAcsClient acsClient;

    @Override
    public void run(String... args) {
        checkUserConfig();
        checkDomainInfo();
        initRRS();
    }

    private void checkUserConfig() {
        logger.info("开始校验用户配置信息...");
        String regionId = ddnsProperties.getRegionId();
        String accessKeyId = ddnsProperties.getAccessKeyId();
        String accessKeySecret = ddnsProperties.getAccessKeySecret();
        String domainName = ddnsProperties.getDomainName();
        String rr = ddnsProperties.getRR();
        Long ttl = ddnsProperties.getTtl();
        try {
            Assert.notNull(regionId, "RegionId must not be null");
            Assert.notNull(accessKeyId, "AccessKeyId must not be null");
            Assert.notNull(accessKeySecret, "AccessKeySecret must not be null");
            Assert.notNull(domainName, "DomainName must not be null");
            Assert.notNull(rr, "RR must not be null");
            Assert.notNull(ttl, "TTL must not be null");
        } catch (Exception e) {
            throw new RuntimeException("校验用户配置信息失败", e);
        }
        logger.info("校验用户配置信息成功...");
    }

    /**
     * 校验域名是否存在 不存在则退出
     */
    private void checkDomainInfo() {
        logger.info("开始校验域名信息...");
        final String domainName = ddnsProperties.getDomainName().trim();

        DescribeDomainsRequest request = new DescribeDomainsRequest();
        request.setKeyWord(domainName);
        // 指定访问协议
        request.setProtocol(ProtocolType.HTTPS);
        // 指定请求方法
        request.setMethod(MethodType.POST);
        try {
            Boolean isExist = Boolean.FALSE;
            DescribeDomainsResponse response = acsClient.getAcsResponse(request);
            List<DescribeDomainsResponse.Domain> list = response.getDomains();
            for (DescribeDomainsResponse.Domain domain : list) {
                if (domain.getDomainName().equals(domainName)) {
                    isExist = Boolean.TRUE;
                }
            }
            if (!isExist) {
                logger.error("该域名 {} 在此账户下不存在,应用退出...", domainName);
                System.exit(1);
            }
        } catch (ClientException e) {
            String errCode = e.getErrCode();
            String invalidRegionIdCode = "SDK.InvalidRegionId";
            if(invalidRegionIdCode.equals(errCode)){
                logger.error("请检查YML文件中 regionId 配置, 当前阿里云只支持 cn-hangzhou ,应用退出...");
            }else{
                logger.error("校验域名信息异常", e);
            }
            throw new RuntimeException("校验用户配置信息失败", e);
        }

        logger.info("校验域名信息成功...");
    }

    /**
     * 初始化所有未配置解析的主机记录
     */
    private void initRRS(){
        final String domainName = ddnsProperties.getDomainName();
        final List<String> rrs = ddnsProperties.getRrs();
        final Long ttl = ddnsProperties.getTtl();
        Assert.notEmpty(rrs, "主机记录不能为空");

        Set<String> existRR = new HashSet<>();

        // 查询改域名所有A主机记录
        DescribeDomainRecordsRequest describeDomainRecordsRequest = new DescribeDomainRecordsRequest();
        describeDomainRecordsRequest.setDomainName(domainName);
        describeDomainRecordsRequest.setTypeKeyWord("A");
        try {
            DescribeDomainRecordsResponse acsResponse = acsClient.getAcsResponse(describeDomainRecordsRequest);
            List<DescribeDomainRecordsResponse.Record> domainRecords = acsResponse.getDomainRecords();
            for (DescribeDomainRecordsResponse.Record domainRecord : domainRecords) {
                logger.info("域名 {} 解析记录类型 {} 主机记录 {} 记录值 {}", domainRecord.getDomainName(), domainRecord.getType(), domainRecord.getRR(), domainRecord.getValue());
                CacheManager.getInstance().putValue(domainRecord.getRR(), domainRecord.getRecordId());
                if("A".equals(domainRecord.getType())){
                    existRR.add(domainRecord.getRR());
                }
            }
        } catch (ClientException e) {
            throw new RuntimeException("获取域名解析记录异常", e);
        }

        for (String rr : rrs) {
            if (!existRR.contains(rr)) {
                // 新增一条解析记录
                AddDomainRecordRequest addDomainRecordRequest = new AddDomainRecordRequest();
                addDomainRecordRequest.setDomainName(domainName);
                addDomainRecordRequest.setRR(rr);
                addDomainRecordRequest.setType("A");
                addDomainRecordRequest.setValue("0.0.0.0");
                addDomainRecordRequest.setTTL(ttl);
                try {
                    AddDomainRecordResponse acsResponse = acsClient.getAcsResponse(addDomainRecordRequest);
                    logger.info("域名 {} 新增解析 {} ", domainName, rr);
                    CacheManager.getInstance().putValue(rr, acsResponse.getRecordId());
                } catch (ClientException e) {
                    logger.error("域名 {} 新增解析 {} 异常,系统退出, 请重试或手动添加改主机记录", domainName, rr);
                    throw new RuntimeException("新增解析异常", e);
                }
            }
        }

    }

}
