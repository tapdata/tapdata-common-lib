package io.tapdata.firedome;

/**
 * @author samuel
 * @Description
 * @create 2025-08-15 15:49
 **/
public interface PrometheusName {
	/* task */
	String TASK_CDC_DELAY_MS = "task_cdc_delay_ms";
	String TASK_ACTIVE_DB = "task_active_db";
	String TASK_STATUS = "task_status";
	String TASK_MILESTONE_STATUS = "task_milestone_status";

	/* node */
	String TASK_NODE_PROCESS_DATA_MS = "task_node_process_data_ms";

	/* inspect task */
	String TASK_VALIDATION_STATUS = "task_validation_status";
}
