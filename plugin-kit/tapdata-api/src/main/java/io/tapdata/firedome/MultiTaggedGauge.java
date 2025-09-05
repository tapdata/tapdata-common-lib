package io.tapdata.firedome;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.*;

public class MultiTaggedGauge {

    private String name;
    private String[] tagNames;
    private MeterRegistry registry;
    private Map<String, DoubleWrapper> wrapperMap = new HashMap<>();
    private Map<String, Gauge> gaugeValues = new HashMap<>();

    public MultiTaggedGauge(String name, MeterRegistry registry, String ... tags) {
        this.name = name;
        this.tagNames = tags;
        this.registry = registry;
    }

    public synchronized void set(double value, String... tagValues) {
        String valuesString = Arrays.toString(tagValues);
        if (tagValues.length != tagNames.length) {
            throw new IllegalArgumentException("Gauge tags mismatch! Expected tags: "
                    + Arrays.toString(tagNames) + ", provided tags: " + valuesString);
        }

        DoubleWrapper number = wrapperMap.get(valuesString);
        if (number == null) {
            number = new DoubleWrapper(value);

            List<Tag> tags = new ArrayList<>(tagNames.length);
            for (int i = 0; i < tagNames.length; i++) {
                tags.add(new ImmutableTag(tagNames[i], tagValues[i]));
            }

            Gauge gauge = Gauge.builder(name, number, DoubleWrapper::getValue)
                    .tags(tags)
                    .register(registry);

            wrapperMap.put(valuesString, number);
            gaugeValues.put(valuesString, gauge);
        } else {
            number.setValue(value);
        }
    }

}
