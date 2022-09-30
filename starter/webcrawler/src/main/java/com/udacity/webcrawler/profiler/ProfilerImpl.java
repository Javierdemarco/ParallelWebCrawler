package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate)
      throws IllegalArgumentException {
    Objects.requireNonNull(klass);
    if (!Arrays.stream(klass.getDeclaredMethods())
             .anyMatch(m -> m.isAnnotationPresent(Profiled.class))) {
      throw new IllegalArgumentException("Pofiled Annotation not present in " + klass.getName());
    }

    // TODO: Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the
    // delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this
    //       method. See
    //       https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.
    ProfilingMethodInterceptor handler =
        new ProfilingMethodInterceptor(clock, state, delegate);
    Object proxy = Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                                        new Class<?>[] {klass}, handler);
    return klass.cast(proxy);
  }

  @Override
  public void writeData(Path path) {
    // TODO: Write the ProfilingState data to the given file path. If a file
    // already exists at that
    //       path, the new data should be appended to the existing file.
    try (Writer w = Files.newBufferedWriter(path, StandardOpenOption.APPEND);) {
     writeData(w);;
    } catch (IOException e) {
      e.getStackTrace();
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + DateTimeFormatter.RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
