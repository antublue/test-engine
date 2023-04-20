package org.antublue.test.engine.internal;

import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

public final class TestDescriptorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDescriptorUtil.class);

    private TestDescriptorUtil() {
        // DO NOTHING
    }

    public static void log(TestDescriptor testDescriptor) {
        LOGGER.info("Test descriptor tree ...");
        LOGGER.info("------------------------");

        log(testDescriptor, 0);

        LOGGER.info("------------------------");
    }

    /**
     * Method to log the test hierarchy
     *
     * @param testDescriptor
     * @param indent
     */
    private static void log(TestDescriptor testDescriptor, int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        Switch.switchType(testDescriptor,
                Switch.switchCase(
                        MethodTestDescriptor.class,
                        testMethodTestDescriptor ->
                                stringBuilder
                                        .append("method -> ")
                                        .append(testMethodTestDescriptor.getTestMethod().getName())
                                        .append("()")),
                Switch.switchCase(
                        ParameterTestDescriptor.class,
                        testEngineParameterTestDescriptor ->
                                stringBuilder
                                        .append("parameter -> ")
                                        .append(testEngineParameterTestDescriptor.getTestParameter())),
                Switch.switchCase(
                        ClassTestDescriptor.class,
                        testClassTestDescriptor ->
                                stringBuilder
                                        .append("class -> ")
                                        .append(testClassTestDescriptor.getTestClass().getName())),
                Switch.switchCase(
                        EngineDescriptor.class,
                        engineDescriptor ->
                                stringBuilder
                                        .append("engine -> ")
                                        .append(engineDescriptor.getDisplayName())));

        LOGGER.info(stringBuilder.toString());

        for (TestDescriptor child : testDescriptor.getChildren()) {
            log(child, indent + 2);
        }
    }
}
