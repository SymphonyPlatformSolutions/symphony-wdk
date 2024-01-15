package com.symphony.bdk.workflow.swadl;

import com.symphony.bdk.workflow.engine.executor.ActivityExecutor;
import com.symphony.bdk.workflow.swadl.v1.activity.BaseActivity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collect known activities (including custom ones) from the classpath.
 */
@Slf4j
@SuppressWarnings("unchecked")
public final class ActivityRegistry {

  private static final Set<Class<? extends BaseActivity>> activityTypes;
  @Getter
  private static final Map<Class<? extends BaseActivity>, Class<? extends ActivityExecutor<? extends BaseActivity>>>
      activityExecutors;

  static {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setScanners(Scanners.SubTypes)
        // this is a bit ugly, but it works faster than scanning the entire classpath and for all contexts (JAR, tests)
        .addUrls(ClasspathHelper.forClassLoader().stream()
            // avoid bot dependencies / pick only lib/ folder
            .filter(a -> a.toString().contains("lib/") && !a.toString().contains("BOOT-INF"))
            .collect(Collectors.toList()))
        .addUrls(ClasspathHelper.forPackage("com.symphony.bdk.workflow"))
        .filterInputsBy(new FilterBuilder().includePattern(".*class")));
    activityTypes = reflections.getSubTypesOf(BaseActivity.class);

    activityExecutors = reflections.getSubTypesOf(ActivityExecutor.class).stream()
        .map(Class.class::cast)
        .collect(Collectors.toMap(ActivityRegistry::findMatchingActivity, Function.identity()));

    log.info("Found these activities: {}", activityTypes.stream()
        .map(Class::getSimpleName)
        .sorted()
        .collect(Collectors.toList()));
    // in TRACE level print the full class names and matching executors
    log.trace("Found these activities: {} and executors: {}", activityTypes, activityExecutors);
  }

  private static Class<? extends BaseActivity> findMatchingActivity(
      Class<? extends ActivityExecutor<? extends BaseActivity>> a) {
    try {
      Type activityType = TypeUtils.getTypeArguments(a, ActivityExecutor.class).values().stream()
          .filter(arg -> {
            try {
              return BaseActivity.class.isAssignableFrom(Class.forName(arg.getTypeName()));
            } catch (ClassNotFoundException e) {
              throw new IllegalStateException("Executor " + a + " should implement ActivityExecutor<Activity>");
            }
          })
          .findFirst()
          .orElseThrow();
      return (Class<? extends BaseActivity>) Class.forName(activityType.getTypeName());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private ActivityRegistry() {
    // singleton class
  }

  public static Set<Class<? extends BaseActivity>> getActivityTypes() {
    return new HashSet<>(activityTypes);
  }
}
