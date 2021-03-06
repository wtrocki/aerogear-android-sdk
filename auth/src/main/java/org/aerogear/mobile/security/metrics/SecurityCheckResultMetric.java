package org.aerogear.mobile.security.metrics;

import android.support.annotation.NonNull;

import org.aerogear.mobile.core.metrics.Metrics;
import org.aerogear.mobile.security.SecurityCheckResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.aerogear.mobile.core.utils.SanityCheck.nonNull;

/**
 * Metric representation of {@link SecurityCheckResult}. This is intended to be used with the
 * {@link org.aerogear.mobile.core.metrics.MetricsService}.
 */
public class SecurityCheckResultMetric implements Metrics {

    private final String identifier;
    private final Map<String, String> data;

    public SecurityCheckResultMetric(@NonNull final SecurityCheckResult result) {
        this.identifier = nonNull(result, "result").getName();
        this.data = getDataFromResult(result);
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public Map<String, String> data() {
        return Collections.unmodifiableMap(data);
    }

    private Map<String, String> getDataFromResult(final SecurityCheckResult result) {
        final Map<String, String> data = new HashMap<>();
        data.put("passed", String.valueOf(result.passed()));
        return data;
    }
}
