package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreationListener {

  private static Logger logger = LoggerFactory.getLogger(CreationListener.class);

  private final PortalSettings portalSettings;
  private final AdminServiceAPI.AppAPI appAPI;
  private final AdminServiceAPI.NamespaceAPI namespaceAPI;

  public CreationListener(
      final PortalSettings portalSettings,
      final AdminServiceAPI.AppAPI appAPI,
      final AdminServiceAPI.NamespaceAPI namespaceAPI) {
    this.portalSettings = portalSettings;
    this.appAPI = appAPI;
    this.namespaceAPI = namespaceAPI;
  }

  @EventListener
  public void onAppCreationEvent(AppCreationEvent event) {
    // 将 App 转成 AppDTO 对象
    AppDTO appDTO = BeanUtils.transform(AppDTO.class, event.getApp());
    // 获得有效的 Env 数组
    List<Env> envs = portalSettings.getActiveEnvs();
    // 循环 Env 数组，调用对应的 Admin Service 的 API ，创建 App 对象。
    for (Env env : envs) {
      try {
        appAPI.createApp(env, appDTO);
      } catch (Throwable e) {
        logger.error("Create app failed. appId = {}, env = {})", appDTO.getAppId(), env, e);
        Tracer.logError(String.format("Create app failed. appId = %s, env = %s", appDTO.getAppId(), env), e);
      }
    }
  }

  @EventListener
  public void onAppNamespaceCreationEvent(AppNamespaceCreationEvent event) {
    // 将 AppNamespace 转成 AppNamespaceDTO 对象
    AppNamespaceDTO appNamespace = BeanUtils.transform(AppNamespaceDTO.class, event.getAppNamespace());
    // 获得有效的 Env 数组
    List<Env> envs = portalSettings.getActiveEnvs();
    // 循环 Env 数组，调用对应的 Admin Service 的 API ，创建 AppNamespace 对象。
    for (Env env : envs) {
      try {
        namespaceAPI.createAppNamespace(env, appNamespace);
      } catch (Throwable e) {
        logger.error("Create appNamespace failed. appId = {}, env = {}", appNamespace.getAppId(), env, e);
        Tracer.logError(String.format("Create appNamespace failed. appId = %s, env = %s", appNamespace.getAppId(), env), e);
      }
    }
  }

}
