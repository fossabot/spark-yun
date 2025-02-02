package com.isxcode.star.agent.run;

import com.isxcode.star.api.agent.pojos.req.PluginReq;
import com.isxcode.star.api.agent.pojos.req.SparkSubmit;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;

public interface AgentService {

	String getMaster(String sparkHomePath) throws IOException;

	/**
	 * @param pluginReq
	 *            插件请求体
	 * @param sparkSubmit
	 *            spark作业提交配置
	 */
	SparkLauncher genSparkLauncher(PluginReq pluginReq, SparkSubmit sparkSubmit, String agentHomePath,
			String sparkHomePath) throws IOException;

	String executeWork(SparkLauncher sparkLauncher) throws IOException;

	String getAppStatus(String appId, String sparkHomePath) throws IOException;

	String getAppLog(String appId, String sparkHomePath) throws IOException;

	String getAppData(String appId, String sparkHomePath) throws IOException;

	void killApp(String appId, String sparkHomePath) throws IOException;
}
