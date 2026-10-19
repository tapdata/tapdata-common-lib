package io.tapdata.entity.conversion.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.filter.TapCodecsFilterManager;
import io.tapdata.entity.conversion.PossibleDataTypes;
import io.tapdata.entity.conversion.TargetTypesGenerator;
import io.tapdata.entity.conversion.UnsupportedTypeFallbackHandler;
import io.tapdata.entity.error.CoreException;
import io.tapdata.entity.error.TapAPIErrorCodes;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.mapping.DefaultExpressionMatchingMap;
import io.tapdata.entity.mapping.TypeExprResult;
import io.tapdata.entity.mapping.type.TapMapping;
import io.tapdata.entity.mapping.type.TapCoefficientFloatMapping;
import io.tapdata.entity.mapping.type.TapFloatingPointMapping;
import io.tapdata.entity.result.ResultItem;
import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapString;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.utils.DataMap;
import io.tapdata.entity.utils.InstanceFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;


@Implementation(value = TargetTypesGenerator.class, buildNumber = 0)
public class TargetTypesGeneratorImpl implements TargetTypesGenerator {
    private static final String TAG = TargetTypesGeneratorImpl.class.getSimpleName();

    public TapResult<LinkedHashMap<String, TapField>> convert(LinkedHashMap<String, TapField> sourceFields, DefaultExpressionMatchingMap targetMatchingMap, TapCodecsFilterManager targetCodecFilterManager) {
        return convert(sourceFields, targetMatchingMap, targetCodecFilterManager, null);
    }
    public TapResult<LinkedHashMap<String, TapField>> convert(LinkedHashMap<String, TapField> sourceFields, DefaultExpressionMatchingMap targetMatchingMap, TapCodecsFilterManager targetCodecFilterManager, Map<String, PossibleDataTypes> findPossibleDataTypes) {
        if(sourceFields == null || targetMatchingMap == null)
            return null;
        TapResult<LinkedHashMap<String, TapField>> finalResult = TapResult.successfully();
        LinkedHashMap<String, TapField> targetFieldMap = new LinkedHashMap<>();
        TapField largestStringMappingField = null;
        TapString cachedTapString = null;

        for(Map.Entry<String, TapField> entry : sourceFields.entrySet()) {
            TapField field = entry.getValue();

            if(field.getTapType() == null)
                throw new CoreException(TapAPIErrorCodes.ERROR_TAP_TYPE_MISSING_ON_FIELD, "Tap type is missing for field {}", field);

            String dataType = null;
            TapResult<String> result = null;
            if (field.getTapType() instanceof TapFloat || field.getTapType() instanceof TapDouble) {
                /*
                 * Floating-point mappings use a strict target hierarchy: TapFloat selects
                 * TapFloat, then TapDouble, then TapNumber; TapDouble selects TapDouble,
                 * then TapNumber.  This branch is isolated so other source types keep
                 * the legacy global score-based selection unchanged.
                 */
                result = calculateFloatingPointBestTypeMapping(field, targetMatchingMap, findPossibleDataTypes);
                if (result != null) {
                    dataType = result.getData();
                }
            }

            if ((field.getTapType() instanceof TapFloat || field.getTapType() instanceof TapDouble)
                    && result != null && !result.isSuccessfully()) {
                finalResult.result(TapResult.RESULT_FAILED);
                if (findPossibleDataTypes != null) {
                    findPossibleDataTypes.computeIfAbsent(field.getName(), name -> new PossibleDataTypes());
                }
                targetFieldMap.put(field.getName(), field.clone().dataType(null));
                continue;
            }

            //User custom codec and the legacy best-codec path.
            if(dataType == null && field.getTapType() != null) {
                dataType = targetCodecFilterManager.getDataTypeByTapType(field.getTapType().getClass());
//                if(dataType != null)
//                    dataType = dataType.toLowerCase();
            }

            //Find best codec
            if(dataType == null) {
                Predicate<TapMapping> mappingFilter = null;
                if (field.getTapType() instanceof TapFloat || field.getTapType() instanceof TapDouble) {
                    mappingFilter = this::isSelectableFloatingPointMapping;
                }
                result = calculateBestTypeMapping(field, targetMatchingMap, findPossibleDataTypes, mappingFilter);
                if(result != null) {
                    dataType = result.getData();
                }
            }
            if(result != null) {
                List<ResultItem> resultItems = result.getResultItems();
                if(resultItems != null) {
                    for(ResultItem resultItem : resultItems) {
                        finalResult.addItem(resultItem);
                    }
                }
            }

            //handle by default, find largest string type as default
            if(dataType == null) {
                if(largestStringMappingField == null) {
                    cachedTapString = new TapString();
                    largestStringMappingField = findLargestStringType(field, targetMatchingMap, cachedTapString);
                }
                if(largestStringMappingField != null) {
                    UnsupportedTypeFallbackHandler unsupportedTypeFallbackHandler = InstanceFactory.instance(UnsupportedTypeFallbackHandler.class);
                    if(unsupportedTypeFallbackHandler != null && targetCodecFilterManager.getCodecsRegistry() != null) {
                        unsupportedTypeFallbackHandler.handle(targetCodecFilterManager.getCodecsRegistry(), field, largestStringMappingField.getDataType(), cachedTapString);
                    }
                }
            }
            String largestStringDataType = largestStringMappingField != null ? largestStringMappingField.getDataType() : null;
            targetFieldMap.put(field.getName(), dataType == null ? field.clone().dataType(largestStringDataType)
                    : field.clone().dataType(dataType));
            if(findPossibleDataTypes != null) {
                PossibleDataTypes possibleDataTypes = findPossibleDataTypes.get(field.getName());
                TapField targetField = targetFieldMap.get(field.getName());
                if(possibleDataTypes == null || possibleDataTypes.getDataTypes() == null) {
                    if(possibleDataTypes == null) {
                        possibleDataTypes = new PossibleDataTypes();
                        findPossibleDataTypes.put(field.getName(), possibleDataTypes);
                    }

                    possibleDataTypes.dataType(targetField.getDataType());
                }
            }
        }
        if(finalResult.getResult() != TapResult.RESULT_FAILED
                && finalResult.getResultItems() != null && !finalResult.getResultItems().isEmpty()) {
            finalResult.result(TapResult.RESULT_SUCCESSFULLY_WITH_WARN);
        }
        return finalResult.data(targetFieldMap);
    }

    private TapField findLargestStringType(TapField originField, DefaultExpressionMatchingMap targetMatchingMap, TapString tapString) {
        if(targetMatchingMap == null || targetMatchingMap.isEmpty())
            return null;
        HitTapMappingContainer hitTapMapping = new HitTapMappingContainer();
        TapField field = originField.clone();//new TapField().tapType(tapString).dataType("LargestString");
        field.tapType(tapString);
        targetMatchingMap.iterate(expressionValueEntry -> {
            TapMapping tapMapping = (TapMapping) expressionValueEntry.getValue().get(TapMapping.FIELD_TYPE_MAPPING);
            if(tapMapping != null && tapMapping.getTo() != null) {
                if(tapMapping.getQueryOnly() != null && tapMapping.getQueryOnly()) {
                    return false;
                }
                if(tapMapping.getTo().equals(tapString.getClass().getSimpleName())) {
                    BigDecimal score = tapMapping.matchingScore(field);
                    if(score.compareTo(BigDecimal.ZERO) > 0) {
                        hitTapMapping.input(expressionValueEntry.getKey(), tapMapping, score);
//                        if(score > hitTapMapping.score) {
//                            hitTapMapping.score = score;
//                            hitTapMapping.hitExpression = expressionValueEntry.getKey();
//                            hitTapMapping.tapMapping = tapMapping;
//                        }
                    }
                }
                return false;
            }
            return false;
        });
        HitTapMapping bestOne = hitTapMapping.getBestOne();
        if(bestOne == null)
            return null;
        TapResult<String> result = bestOne.tapMapping.fromTapType(bestOne.hitExpression, field.getTapType());
        List<ResultItem> resultItems = result.getResultItems();
        if(resultItems != null && !resultItems.isEmpty()) {
            for(ResultItem resultItem : resultItems) {
                resultItem.setInformation(resultItem.getItem() + ": " + resultItem.getInformation());
                resultItem.setItem(field.getName());
                TapLogger.debug(TAG, "findLargestStringMapping " + resultItem.getItem() + ": " + resultItem.getInformation());
            }
        }
        field.setDataType(result.getData());
        return field;
    }

    static class HitTapMapping {
        String hitExpression;
        TapMapping tapMapping;
        BigDecimal score = TapMapping.MIN_SCORE;

        public HitTapMapping(String hitExpression, TapMapping tapMapping, BigDecimal score) {
            this.hitExpression = hitExpression;
            this.tapMapping = tapMapping;
            this.score = score;
        }
    }

    static class HitTapMappingContainer {
        TreeMap<Integer, HitTapMapping> sortedMap = new TreeMap<>();
        HitTapMapping bestOne = null;
        TreeMap<BigDecimal, HitTapMapping> justSortedMap = new TreeMap<>();

        void input(String hitExpression, TapMapping tapMapping, BigDecimal score) {
            HitTapMapping hitTapMapping = new HitTapMapping(hitExpression, tapMapping, score);
            if(bestOne == null || score.compareTo(bestOne.score) > 0) {
                sortedMap.clear();
                bestOne = hitTapMapping;
                sortedMap.put(tapMapping.getPriority(), bestOne);
            } else if(score.equals(bestOne.score)) {
                sortedMap.put(tapMapping.getPriority(), hitTapMapping);
            }
            justSortedMap.put(score, hitTapMapping);
        }

        HitTapMapping getBestOne() {
            Map.Entry<Integer, HitTapMapping> entry = sortedMap.firstEntry();
            if(entry != null)
                return entry.getValue();
            return null;
        }
    }

    TapResult<String> calculateBestTypeMapping(TapField field, DefaultExpressionMatchingMap matchingMap, Map<String, PossibleDataTypes> findPossibleDataTypes) {
        return calculateBestTypeMapping(field, matchingMap, findPossibleDataTypes, null);
    }

    private TapResult<String> calculateBestTypeMapping(TapField field, DefaultExpressionMatchingMap matchingMap,
                                                       Map<String, PossibleDataTypes> findPossibleDataTypes,
                                                       Predicate<TapMapping> mappingFilter) {
        HitTapMappingContainer bestTapMapping = new HitTapMappingContainer();
        HitTapMappingContainer bestNotHitTapMapping = new HitTapMappingContainer();

        TreeMap<BigDecimal, Map.Entry<String, DataMap>> sortedTypes = null;
        if(findPossibleDataTypes != null)
            sortedTypes = new TreeMap<>();
//        AtomicReference<String> hitExpression = new AtomicReference<>();
//        AtomicReference<TapMapping> tapMappingReference = new AtomicReference<>();
//        AtomicLong bestScore = new AtomicLong(-1);
//        List<Container<BigDecimal, String>> qualifiedList = new ArrayList<>();
//        List<Container<BigDecimal, String>> noneQualifiedList = new ArrayList<>();
        TreeMap<BigDecimal, Map.Entry<String, DataMap>> finalSortedTypes = sortedTypes;
        matchingMap.iterate(expressionValueEntry -> {
            TapMapping tapMapping = (TapMapping) expressionValueEntry.getValue().get(TapMapping.FIELD_TYPE_MAPPING);
            if(tapMapping != null) {
                if(mappingFilter != null && !mappingFilter.test(tapMapping)) {
                    return false;
                }
                if(tapMapping.getQueryOnly() != null && tapMapping.getQueryOnly()) {
                    return false;
                }

                BigDecimal score = tapMapping.matchingScore(field);
                if(finalSortedTypes != null && TapMapping.MIN_SCORE.compareTo(score) < 0)
                    finalSortedTypes.put(score, expressionValueEntry);
                if(score.compareTo(BigDecimal.ZERO) >= 0) {
//                    qualifiedList.add(new Container<>(score, expressionValueEntry.getKey()));
                    bestTapMapping.input(expressionValueEntry.getKey(), tapMapping, score);
//                    if(score > bestTapMapping.score) {
//                        bestTapMapping.score = score;
//                        bestTapMapping.hitExpression = expressionValueEntry.getKey();
//                        bestTapMapping.tapMapping = tapMapping;
//                    }
                } else {
//                    noneQualifiedList.add(new Container<>(score, expressionValueEntry.getKey()));
                    bestNotHitTapMapping.input(expressionValueEntry.getKey(), tapMapping, score);
//                    if(score > bestNotHitTapMapping.score) {
//                        bestNotHitTapMapping.score = score;
//                        bestNotHitTapMapping.hitExpression = expressionValueEntry.getKey();
//                        bestNotHitTapMapping.tapMapping = tapMapping;
//                    }
                }
                return false;
            }
            return false;
        });
//        qualifiedList.sort((o1, o2) -> o2.getT().compareTo(o1.getT()));
//        noneQualifiedList.sort((o1, o2) -> o2.getT().compareTo(o1.getT()));
//        TapLogger.info(TAG, "Field {} qualified data types {}, not qualified data types {}", field.getDataType(),
//                qualifiedList.stream().map(Container::getP).collect(Collectors.toList()),
//                noneQualifiedList.stream().map(Container::getP).collect(Collectors.toList()));
        if(findPossibleDataTypes != null) {
            PossibleDataTypes possibleDataTypes = findPossibleDataTypes.get(field.getName());
            if(possibleDataTypes == null)
                findPossibleDataTypes.computeIfAbsent(field.getName(), name -> new PossibleDataTypes());
            possibleDataTypes = findPossibleDataTypes.get(field.getName());
            for(Map.Entry<BigDecimal, Map.Entry<String, DataMap>> entry : sortedTypes.entrySet()) {
                TapMapping tapMapping = (TapMapping) entry.getValue().getValue().get(TapMapping.FIELD_TYPE_MAPPING);
                if(tapMapping != null) {
                    TapResult<String> result = tapMapping.fromTapType(entry.getValue().getKey(), field.getTapType());
                    if(result != null && result.isSuccessfully() && result.getData() != null) {
                        possibleDataTypes.dataType(result.getData());
                        if(possibleDataTypes.getLastMatchedDataType() == null && entry.getKey().compareTo(BigDecimal.ZERO) >= 0) {
                            possibleDataTypes.setLastMatchedDataType(result.getData());
                        }
                    }
                }
            }
        }
        HitTapMapping bestOne = bestTapMapping.getBestOne();
        if(bestOne != null && bestOne.tapMapping != null && bestOne.hitExpression != null) {
            TapResult<String> tapResult = bestOne.tapMapping.fromTapType(bestOne.hitExpression, field.getTapType());
            if(tapResult != null) {
                List<ResultItem> resultItems = tapResult.getResultItems();
                if(resultItems != null) {
                    for(ResultItem resultItem : resultItems) {
                        resultItem.setInformation(resultItem.getItem() + ": " + resultItem.getInformation());
                        resultItem.setItem(field.getName());
                    }
                }
            }
            return tapResult;
        }
        HitTapMapping notHitBestOne = bestNotHitTapMapping.getBestOne();
        if(notHitBestOne != null && notHitBestOne.tapMapping != null && notHitBestOne.hitExpression != null) {
            TapResult<String> tapResult = notHitBestOne.tapMapping.fromTapType(notHitBestOne.hitExpression, field.getTapType());
            if(tapResult != null) {
                List<ResultItem> resultItems = tapResult.getResultItems();
                if(resultItems != null) {
                    for(ResultItem resultItem : resultItems) {
                        resultItem.setInformation(resultItem.getItem() + ": " + resultItem.getInformation());
                        resultItem.setItem(field.getName());
                    }
                }
                tapResult.addItem(new ResultItem(field.getName(), TapResult.RESULT_SUCCESSFULLY_WITH_WARN, "BEST_IN_UNMATCHED: " + "Select best in unmatched TapMapping, " + notHitBestOne.hitExpression));
            }
            return tapResult;
        }
        return null;
    }

    private TapResult<String> calculateFloatingPointBestTypeMapping(TapField field,
                                                                      DefaultExpressionMatchingMap matchingMap,
                                                                      Map<String, PossibleDataTypes> findPossibleDataTypes) {
        boolean declaredTierExists = false;
        if (field.getTapType() instanceof TapFloat) {
            FloatingPointTierSelection selection = calculateFloatingPointTierMapping(field, matchingMap,
                    findPossibleDataTypes, FloatingPointTargetTier.TAP_FLOAT);
            declaredTierExists = selection.exists;
            if (!selection.candidates.isEmpty()) {
                return selection.result;
            }
            if (selection.exists) {
                TapLogger.warn(TAG, "No usable TapFloat target candidate for field " + field.getName() + ", try TapDouble.");
            }

            selection = calculateFloatingPointTierMapping(field, matchingMap, findPossibleDataTypes,
                    FloatingPointTargetTier.TAP_DOUBLE);
            declaredTierExists = declaredTierExists || selection.exists;
            if (!selection.candidates.isEmpty()) {
                return selection.result;
            }
            if (selection.exists) {
                TapLogger.warn(TAG, "No usable TapDouble target candidate for field " + field.getName() + ", try TapNumber.");
            }
        } else if (field.getTapType() instanceof TapDouble) {
            FloatingPointTierSelection selection = calculateFloatingPointTierMapping(field, matchingMap,
                    findPossibleDataTypes, FloatingPointTargetTier.TAP_DOUBLE);
            declaredTierExists = selection.exists;
            if (!selection.candidates.isEmpty()) {
                return selection.result;
            }
            if (selection.exists) {
                TapLogger.warn(TAG, "No usable TapDouble target candidate for field " + field.getName() + ", try TapNumber.");
            }
        }

        FloatingPointTierSelection selection = calculateFloatingPointTierMapping(field, matchingMap,
                findPossibleDataTypes, FloatingPointTargetTier.TAP_NUMBER);
        declaredTierExists = declaredTierExists || selection.exists;
        if (!selection.candidates.isEmpty()) {
            return selection.result;
        }
        if (selection.exists) {
            TapLogger.warn(TAG, "No usable TapNumber target candidate for field " + field.getName() + ".");
            return selection.result;
        }
        return declaredTierExists ? TapResult.failed() : null;
    }

    private FloatingPointTierSelection calculateFloatingPointTierMapping(TapField field,
                                                                           DefaultExpressionMatchingMap matchingMap,
                                                                           Map<String, PossibleDataTypes> findPossibleDataTypes,
                                                                           FloatingPointTargetTier targetTier) {
        FloatingPointTierSelection selection = new FloatingPointTierSelection();
        matchingMap.iterateInDefinitionOrder(expressionValueEntry -> {
            TapMapping tapMapping = (TapMapping) expressionValueEntry.getValue().get(TapMapping.FIELD_TYPE_MAPPING);
            if (!isSelectableFloatingPointMapping(tapMapping)
                    || !isTargetTierMapping(tapMapping, targetTier)) {
                return false;
            }

            selection.exists = true;
            BigDecimal score = tapMapping.matchingScore(field);
            TapResult<String> result;
            try {
                result = tapMapping.fromTapType(expressionValueEntry.getKey(), field.getTapType());
            } catch (RuntimeException exception) {
                TapLogger.warn(TAG, "Find tapType failed: " + expressionValueEntry.getKey() + ", field TapType is " + field.getTapType().getTypeName() + ", message: " + exception.getMessage());
                return false;
            }
            if (isUsableFloatingPointCandidate(result, matchingMap, targetTier)) {
                selection.candidates.add(new FloatingPointCandidate(score, priorityOf(tapMapping),
                        selection.definitionOrder++, result));
            }
            return false;
        });

        selection.candidates.sort((left, right) -> {
            int scoreCompare = right.score.compareTo(left.score);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            int priorityCompare = Integer.compare(left.priority, right.priority);
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            return Integer.compare(left.definitionOrder, right.definitionOrder);
        });

        updateFloatingPointPossibleDataTypes(field, findPossibleDataTypes, selection.candidates);
        selection.result = selection.candidates.isEmpty()
                ? TapResult.failed()
                : selection.candidates.get(0).result;
        return selection;
    }

    private boolean isUsableFloatingPointCandidate(TapResult<String> result,
                                                    DefaultExpressionMatchingMap matchingMap,
                                                    FloatingPointTargetTier targetTier) {
        if (result == null || !result.isSuccessfully() || result.getData() == null) {
            return false;
        }
        String dataType = result.getData().trim();
        if (dataType.isEmpty() || dataType.indexOf('$') >= 0 || dataType.indexOf('[') >= 0
                || dataType.indexOf(']') >= 0) {
            return false;
        }

        TypeExprResult<DataMap> reparsed = matchingMap.get(dataType);
        if (reparsed == null || reparsed.getValue() == null) {
            return false;
        }
        TapMapping reparsedMapping = (TapMapping) reparsed.getValue().get(TapMapping.FIELD_TYPE_MAPPING);
        if (!isSelectableFloatingPointMapping(reparsedMapping)
                || !isTargetTierMapping(reparsedMapping, targetTier)) {
            return false;
        }
        try {
            boolean expectedTier = isTargetTierType(reparsedMapping.toTapType(dataType, reparsed.getParams()), targetTier);
            if (expectedTier) {
                result.data(dataType);
            }
            return expectedTier;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void updateFloatingPointPossibleDataTypes(TapField field,
                                                       Map<String, PossibleDataTypes> findPossibleDataTypes,
                                                       List<FloatingPointCandidate> candidates) {
        if (findPossibleDataTypes == null || candidates.isEmpty()) {
            return;
        }
        PossibleDataTypes possibleDataTypes = findPossibleDataTypes.computeIfAbsent(field.getName(),
                name -> new PossibleDataTypes());
        for (FloatingPointCandidate candidate : candidates) {
            possibleDataTypes.dataType(candidate.result.getData());
        }
        if (possibleDataTypes.getLastMatchedDataType() == null) {
            possibleDataTypes.setLastMatchedDataType(candidates.get(0).result.getData());
        }
    }

    private int priorityOf(TapMapping mapping) {
        return mapping.getPriority() == null ? Integer.MAX_VALUE : mapping.getPriority();
    }

    private boolean isSelectableFloatingPointMapping(TapMapping mapping) {
        return mapping != null
                && !Boolean.TRUE.equals(mapping.getQueryOnly())
                && !Boolean.TRUE.equals(mapping.getCannotWrite());
    }

    private boolean isTargetTierMapping(TapMapping mapping, FloatingPointTargetTier targetTier) {
        switch (targetTier) {
            case TAP_FLOAT:
                if (mapping instanceof TapCoefficientFloatMapping) {
                    return ((TapCoefficientFloatMapping) mapping).supportsTapFloatTarget();
                }
                if (mapping instanceof TapFloatingPointMapping) {
                    return ((TapFloatingPointMapping) mapping).supportsTapFloatTarget();
                }
                return "TapFloat".equals(mapping.getTo());
            case TAP_DOUBLE:
                if (mapping instanceof TapCoefficientFloatMapping) {
                    return ((TapCoefficientFloatMapping) mapping).supportsTapDoubleTarget();
                }
                if (mapping instanceof TapFloatingPointMapping) {
                    return ((TapFloatingPointMapping) mapping).supportsTapDoubleTarget();
                }
                return "TapDouble".equals(mapping.getTo());
            case TAP_NUMBER:
                return "TapNumber".equals(mapping.getTo());
            default:
                return false;
        }
    }

    private boolean isTargetTierType(TapType tapType, FloatingPointTargetTier targetTier) {
        switch (targetTier) {
            case TAP_FLOAT:
                return tapType instanceof TapFloat;
            case TAP_DOUBLE:
                return tapType instanceof TapDouble;
            case TAP_NUMBER:
                return tapType instanceof TapNumber
                        && !(tapType instanceof TapFloat)
                        && !(tapType instanceof TapDouble);
            default:
                return false;
        }
    }

    private enum FloatingPointTargetTier {
        TAP_FLOAT,
        TAP_DOUBLE,
        TAP_NUMBER
    }

    private static class FloatingPointTierSelection {
        boolean exists;
        int definitionOrder;
        List<FloatingPointCandidate> candidates = new ArrayList<>();
        TapResult<String> result;
    }

    private static class FloatingPointCandidate {
        final BigDecimal score;
        final int priority;
        final int definitionOrder;
        final TapResult<String> result;

        FloatingPointCandidate(BigDecimal score, int priority, int definitionOrder, TapResult<String> result) {
            this.score = score == null ? TapMapping.MIN_SCORE : score;
            this.priority = priority;
            this.definitionOrder = definitionOrder;
            this.result = result;
        }
    }


}
