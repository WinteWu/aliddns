package com.wintewu.aliddns.scheduler;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.wintewu.aliddns.common.IPUtils;
import com.wintewu.aliddns.common.cache.CacheManager;
import com.wintewu.aliddns.common.constant.AliddnsConstant;
import com.wintewu.aliddns.config.properties.DDNSProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 阿里云ddns定时任务
 * @author wintewu
 */
@Component
public class AliddnsUpdateTask {

    private static Logger logger = LoggerFactory.getLogger(AliddnsUpdateTask.class);

    @Autowired
    private DDNSProperties ddnsProperties;

    @Autowired
    private IAcsClient acsClient;


    @Scheduled(cron = "0/5 * * * * ? ")
    public void execute(){
        final String IP = IPUtils.getIP();
        if (StringUtils.isBlank(IP)) {
            logger.error("获取本机公网IP失败,任务跳过...");
            return;
        }

        final String oldIp = CacheManager.getInstance().getStringValueByKey(AliddnsConstant.IP_KEY);
        // 如果缓存中IP与公网IP不相等 则更新解析 应用启动必执行一次修改解析
        if (!IP.equals(oldIp)) {
            List<String> rrs = ddnsProperties.getRrs();
            Long ttl = ddnsProperties.getTtl();
            for (String rr : rrs) {
                String rrRecordId = CacheManager.getInstance().getStringValueByKey(rr);
                if (StringUtils.isBlank(rrRecordId)) {
                    logger.error("域名 {} 主机记录 {} 不存在RecordId 请重启应用.任务跳过...");
                    return;
                }

                UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
                updateDomainRecordRequest.setRecordId(rrRecordId);
                updateDomainRecordRequest.setRR(rr);
                updateDomainRecordRequest.setType("A");
                updateDomainRecordRequest.setValue(IP);
                updateDomainRecordRequest.setTTL(ttl);
                try {
                    UpdateDomainRecordResponse acsResponse = acsClient.getAcsResponse(updateDomainRecordRequest);
                    logger.info("修改域名 {} 主机记录{} 值为 {}", ddnsProperties.getDomainName(), rr, IP);
                } catch (ClientException e) {
                    String errCode = e.getErrCode();
                    if (errCode.equals("DomainRecordDuplicate")) {
                        logger.info("域名 {} 主机记录{} 解析记录已存在 跳过...", ddnsProperties.getDomainName(), rr);
                    } else if (errCode.equals("DomainRecordConflict")) {
                        logger.info("域名 {} 主机记录{} 解析记录冲突 跳过...", ddnsProperties.getDomainName(), rr);
                    } else if (errCode.equals("DomainForbidden")) {
                        logger.info("域名 {} 主机记录{} 禁止解析操作的域名 跳过...", ddnsProperties.getDomainName(), rr);
                    } else if (errCode.equals("UnKnownError")) {
                        logger.info("域名 {} 主机记录{} 未知错误 跳过...", ddnsProperties.getDomainName(), rr);
                    } else {
                        logger.info("修改域名 {} 主机记录{} 异常 跳过...", ddnsProperties.getDomainName(), rr, e);
                    }
                }
            }
            CacheManager.getInstance().putValue(AliddnsConstant.IP_KEY, IP);
        } else {
            logger.debug("IP未改变,当前IP：{}", IP);
        }
    }
}
