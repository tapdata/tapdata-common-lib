package io.tapdata.entity.simplify.pretty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author samuel
 * @Description
 * @create 2024-02-22 15:05
 **/
public class ClassHandlersV2 {
	private static final String TAG = ClassHandlers.class.getSimpleName();
	private final Map<String, List<Function<?, ?>>> classHandlersMap = new ConcurrentHashMap<>();

	public List<String> keyList() {
		return new ArrayList<>(classHandlersMap.keySet());
	}
	public <T, R> void register(Class<T> tClass, Function<T, R> objectHandler) {
		List<Function<?, ?>> objectHandlers = classHandlersMap.compute(tClass.getName(), (aClass, classObjectHandlers) -> new ArrayList<>());
		objectHandlers.add(objectHandler);
	}

	public Object handle(Object t) {
		if(t != null) {
			List<Function<?, ?>> objectHandlers = classHandlersMap.get(t.getClass().getName());
			if(objectHandlers != null) {
				for(Function objectHandler : objectHandlers) {
					Object result = objectHandler.apply(t);
					if(result != null)
						return result;
				}
			}
		}
		return null;
	}
}
