package io.tapdata.common.sample;

import io.tapdata.firedome.MultiTaggedGauge;

/**
 * @author samuel
 * @Description
 * @create 2025-08-15 15:09
 **/
public interface SamplerPrometheus extends Sampler {

	default String[] tagValues() {
		return null;
	}

	default MultiTaggedGauge multiTaggedGauge() {
		return null;
	}
}
